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
import dk.dma.common.exception.ErrorMessage;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.net.ftp.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.file.*;
import java.time.Instant;
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
    private static final Pattern FOLDER_PATTERN = Pattern.compile("\\d{10}");
    private static final int MAX_TRIES = 4;
    private final WeatherService gridWeatherService;

    private final String tempDirLocation;
    private final List<ForecastConfiguration> configurations;
    private File lastTempDir;
    private String hostname = "ftp.dmi.dk";

    @Autowired
    public FTPLoader(WeatherService gridWeatherService, @Value("${ftploader.tempdir:#{null}}") String tempDirLocation, List<ForecastConfiguration> configurations) {
        this.gridWeatherService = gridWeatherService;
        this.tempDirLocation = (tempDirLocation != null ? tempDirLocation : System.getProperty("java.io.tmpdir"));
        this.configurations = configurations;
    }

    /**
     * Check for files every 10 minutes. New files are given to the gridWeatherService
     */
    @Scheduled(initialDelay = 1000, fixedDelay = 10 * 60 * 1000)
    public void checkFiles() {
        log.info("Checking FTP files at DMI.");
        FTPClient client = new FTPClient();
        try {
            client.setDataTimeout(20 * 1000);
            client.setBufferSize(1024*1024);
            client.connect(hostname);
            if (client.login("anonymous", "")) {
                try {
                    client.enterLocalPassiveMode();
                    client.setFileType(FTP.BINARY_FILE_TYPE);
                    for (ForecastConfiguration configuration : configurations) {
                        // DMI creates a Newest link once all files have been created
                        if (client.changeWorkingDirectory(configuration.getFolder() + "/Newest")) {
                            if (client.getReplyCode() != 250) {
                                log.error("Did not get reply 250 as expected, got {} ", client.getReplyCode());
                            }
                            String workingDirectory = new File(client.printWorkingDirectory()).getName();

                            FTPFile[] listFiles = client.listFiles();
                            List<FTPFile> files = Arrays.stream(listFiles).filter(f -> configuration.getFilePattern().matcher(f.getName()).matches()).collect(Collectors.toList());

                            try {
                                Map<File, Instant> localFiles = transferFilesIfNeeded(client, workingDirectory, files);
                                gridWeatherService.newFiles(localFiles, configuration);
                                File newTempDir = localFiles.keySet().iterator().next().getParentFile();
                                if (lastTempDir != null && newTempDir.equals(lastTempDir)) {
                                    deleteRecursively(lastTempDir);
                                    lastTempDir = newTempDir;
                                }
                            } catch (IOException e) {
                                log.warn("Unable to get new weather files from DMI", e);
                            }

                        } else {
                            gridWeatherService.setErrorMessage(ErrorMessage.FTP_PROBLEM);
                            log.error("Unable to change ftp directory to {}", configuration.getFolder());
                        }
                    }
                } finally {
                    try {
                        client.logout();
                    } catch (IOException e) {
                        log.info("Failed to logout", e);
                    }
                }
            } else {
                gridWeatherService.setErrorMessage(ErrorMessage.FTP_PROBLEM);
                log.error("Unable to login to {}", hostname);
            }

        } catch (IOException e) {
            gridWeatherService.setErrorMessage(ErrorMessage.FTP_PROBLEM);
            log.error("Unable to update weather files from DMI", e);
        } finally {
            try {
                client.disconnect();
            } catch (IOException e) {
                log.info("Failed to disconnect", e);
            }
        }
    }

    /**
     * Copied the files from DMIs ftp server to the local machine
     *
     * @return a Map with a local file and the time the file was created on the FTP server
     */
    private Map<File, Instant> transferFilesIfNeeded(FTPClient client, String directoryName, List<FTPFile> files) throws IOException {

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
        Map<File, Instant> transferred = new HashMap<>();
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
                    transferred.put(tmp, file.getTimestamp().toInstant());
                    continue;
                }
            }
            if (tmp.createNewFile()) {
                log.info("downloading {}", tmp.getName());

                try (FileOutputStream fout = new FileOutputStream(tmp)) {
                    // this often fails with java.net.ConnectException: Operation timed out
                    int count = 0;
                    while (count++ < MAX_TRIES) {
                        try {
                            client.retrieveFile(file.getName(), fout);
                            break;
                        } catch (IOException e) {
                            log.warn(String.format("Failed to transfer file %s, try number %s", file.getName(), count), e);
                        }
                    }
                    fout.flush();
                }
            } else {
                throw new IOException("Unable to create temp file on disk.");
            }

            transferred.put(tmp, file.getTimestamp().toInstant());
        }
        log.info("transferred weather files in {} ms", stopwatch.stop().elapsed(TimeUnit.MILLISECONDS));
        return transferred;
    }

    private static void deleteRecursively(File lastTempDir) throws IOException {
        Path rootPath = lastTempDir.toPath();
        //noinspection ResultOfMethodCallIgnored
        Files.walk(rootPath, FileVisitOption.FOLLOW_LINKS)
                .sorted(Comparator.reverseOrder())  // flips the tree so leefs are deleted first
                .map(Path::toFile)
                .peek(f -> log.debug("deleting file " + f.getAbsolutePath()))
                .forEach(File::delete);

    }

}
