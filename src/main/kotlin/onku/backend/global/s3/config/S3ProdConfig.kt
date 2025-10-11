package onku.backend.global.s3.config

import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.AmazonS3ClientBuilder
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

@Configuration
@Profile("prod")
class S3ProdConfig(
    @Value("\${cloud.aws.region.static}") private val region: String
) {
    @Bean
    fun prodAmazonS3Client(): AmazonS3 =
        AmazonS3ClientBuilder.standard()
            .withRegion(region)
            .build()
}