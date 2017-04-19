package dk.dma.nogoservice.service;

import com.amazonaws.services.s3.AmazonS3;
import dk.dma.nogoservice.ApiProfiles;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * @author Klaus Groenbaek
 *         Created 19/04/2017.
 */
@Component
@Profile(ApiProfiles.PRODUCTION)
public class SouthKattegatQueryArea extends S3FileBackedQueryArea {

    @Autowired
    public SouthKattegatQueryArea(AmazonS3 amazonS3, WeatherService weatherService, NoGoAlgorithmFacade noGoAlgorithm) {
        super("South Kattegat",amazonS3, 	"SouthKattegat_depth.json", weatherService, noGoAlgorithm);
    }


}
