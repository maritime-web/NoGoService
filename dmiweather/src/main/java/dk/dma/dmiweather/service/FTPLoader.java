/* Copyright (c) 2011 Danish Maritime Authority.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package dk.dma.dmiweather.service;

import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;
import com.google.common.io.PatternFilenameFilter;
import dk.dma.dmiweather.dto.ErrorMessage;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.net.ftp.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Class that loads DMI weather data GRIB1 files from ftp. Files are stored locally in a temp folder.
 * DMI uploads new files to the FTP server every 6 hours, when we detect this we download the new files,
 * and sends their location to the GridWeatherService, which handles the data queries
 *
 * @author Klaus Groenbaek
 *         Created 29/03/17.
 */
@Component
@Slf4j
public class FTPLoader {
    /**
     * location of the GRIB1 file2 with danish weather, the folder also contains the Baltic and North sea
     */
    private static final String DANISH_WEATHER_FOLDER = "/mhri/EfficientSea/metocean_shelf";
    public static final Pattern FOLDER_PATTERN = Pattern.compile("\\d{10}");
    public static final Pattern DENMARK_FILE_PATTERN = Pattern.compile("DMI_metocean_DK\\.(\\d{10})\\.grb");

    private static final int FILE_COUNT = 121;
    private final WeatherServiceImpl gridWeatherService;

    private String tempDirLocation;
    /**
     * Use a known temp dir so we don't have to download the files from FTP when developing
     */
    private String newestDirectoryName;
    private File lastTempDir;
    private String hostname = "ftp.dmi.dk";

    @Autowired
    public FTPLoader(WeatherServiceImpl gridWeatherService, @Value("${ftploader.tempdir:#{null}}") String tempDirLocation) {
        this.gridWeatherService = gridWeatherService;
        this.tempDirLocation = (tempDirLocation != null ? tempDirLocation : System.getProperty("java.io.tmpdir"));
    }

    /**
     * Check for files every 10 minutes. New files are given to the gridWeatherService
     */
    @Scheduled(initialDelay = 1000, fixedDelay = 10 * 60 * 1000)
    public void checkFiles() {

        try {
            log.info("Checking FTP files at DMI.");
            FTPClient client = new FTPClient();
            client.connect(hostname);
            if (client.login("anonymous", "")) {
                client.enterLocalPassiveMode();
                client.setFileType(FTP.BINARY_FILE_TYPE);
                if (client.changeWorkingDirectory(DANISH_WEATHER_FOLDER)) {
                    FTPFile[] ftpDirs = client.listDirectories();
                    Arrays.sort(ftpDirs, new FileAgeComparator());
                    FTPFile newestDirectory = ftpDirs[ftpDirs.length - 1];
                    if (client.changeWorkingDirectory(newestDirectory.getName())) {
                        FTPFile[] listFiles = client.listFiles();
                        List<FTPFile> files = Arrays.stream(listFiles).filter(f -> DENMARK_FILE_PATTERN.matcher(f.getName()).matches()).collect(Collectors.toList());
                        if (files.size() == FILE_COUNT) {
                            // we have a full set of files 5 day of 24 hours + 1 hour,
                            // this is Important since FTP is not transaction and we could read while files are being copied
                            if (!newestDirectory.getName().equals(newestDirectoryName)) {
                                // this is the first time we encounter this directory after it has all the files
                                try {
                                    List<File> localFiles = transferFilesIfNeeded(client, newestDirectory.getName(), files);
                                    gridWeatherService.newFiles(localFiles);
                                    File newTempDir = localFiles.get(0).getParentFile();
                                    if (lastTempDir != null && newTempDir.equals(lastTempDir)) {
                                        deleteRecursively(lastTempDir);
                                        lastTempDir = newTempDir;
                                    }
                                    newestDirectoryName = newestDirectory.getName();
                                } catch (IOException e) {
                                    log.warn("Unable to get new weather files from DMI", e);
                                }
                            } else {
                                log.info("Newest directory is still named {}", newestDirectory.getName());
                            }
                        } else {
                            log.warn("Skipping new directory because it only has {} files", files.size());
                        }
                    } else {
                        gridWeatherService.setErrorMessage(ErrorMessage.FTP_PROBLEM);
                        log.error("Unable to change directory to {}", newestDirectory.getName());
                    }
                } else {
                    gridWeatherService.setErrorMessage(ErrorMessage.FTP_PROBLEM);
                    log.error("Unable to change ftp directory to {}", DANISH_WEATHER_FOLDER);
                }
            } else {
                gridWeatherService.setErrorMessage(ErrorMessage.FTP_PROBLEM);
                log.error("Unable to login to {}", hostname);
            }
            client.logout();
            client.disconnect();

        } catch (IOException e) {
            gridWeatherService.setErrorMessage(ErrorMessage.FTP_PROBLEM);
            log.error("Unable to update weather files from DMI", e);
        }

    }

    /**
     * Copied the files from DMIs ftp server to the local machine
     */
    private List<File> transferFilesIfNeeded(FTPClient client, String directoryName, List<FTPFile> files) throws IOException {

        File current = new File(tempDirLocation, directoryName);
        if (lastTempDir == null) {
            // If we just started check if there is data from an earlier run and delete it
            File temp = new File(tempDirLocation);
            if (temp.exists()) {
                File[] oldFolders = temp.listFiles(new PatternFilenameFilter(FOLDER_PATTERN));
                if (oldFolders != null) {
                    List<File> foldersToDelete = Lists.newArrayList(oldFolders);
                    foldersToDelete.remove(current);
                    for (File oldFolder : foldersToDelete) {
                        log.info("deleting old GRIB folder {}", oldFolder);
                        deleteRecursively(oldFolder);
                    }
                }
            }
        }

        if (!current.exists()) {
            if (!current.mkdirs()) {
                throw new IOException("Unable to create temp directory " + current.getAbsolutePath());
            }
        }


        Stopwatch stopwatch = Stopwatch.createStarted();
        List<File> transferred = new ArrayList<>();
        for (FTPFile file : files) {
            File tmp = new File(current, file.getName());
            if (tmp.exists()) {
                long localSize = Files.size(tmp.toPath());
                if (localSize != file.getSize()) {
                    log.info("deleting {} local file has size {}, remote is {}", tmp.getName(), localSize, file.getSize());
                    if (!tmp.delete()) {
                        log.warn("Unable to delete " + tmp.getAbsolutePath());
                    }
                } else {
                    // If the file has the right size we assume it was copied correctly (otherwise we needed to hash them)
                    log.info("Reusing already downloaded version of {}", tmp.getName());
                    transferred.add(tmp);
                    continue;
                }
            }
            if (tmp.createNewFile()) {
                log.info("downloading {}", tmp.getName());

                try (FileOutputStream fout = new FileOutputStream(tmp)) {
                    // must be set every time otherwise we will get ASCII substitution (the JavaDoc for the method is wrong or the DMI behaves strangely)
                    client.retrieveFile(file.getName(), fout);
                }
            } else {
                throw new IOException("Unable to create temp file on disk.");
            }

            transferred.add(tmp);
        }
        log.info("transferred weather files in {} ms", stopwatch.stop().elapsed(TimeUnit.MILLISECONDS));
        return transferred;
    }

    public static void deleteRecursively(File lastTempDir) throws IOException {
        Path rootPath = lastTempDir.toPath();
        Files.walk(rootPath, FileVisitOption.FOLLOW_LINKS)
                .sorted(Comparator.reverseOrder())  // flips the tree so leefs are deleted first
                .map(Path::toFile)
                .peek(f -> log.debug("deleting file " + f.getAbsolutePath()))
                .forEach(File::delete);

    }

    private static class FileAgeComparator implements Comparator<FTPFile>, Serializable {
        @Override
        public int compare(FTPFile o1, FTPFile o2) {
            return o1.getTimestamp().compareTo(o2.getTimestamp());
        }
    }
}
