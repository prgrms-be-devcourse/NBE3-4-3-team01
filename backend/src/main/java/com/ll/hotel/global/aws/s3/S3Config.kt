package com.ll.hotel.global.aws.s3

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.presigner.S3Presigner

@Configuration
class S3Config(
    @Value("\${spring.cloud.aws.credentials.access-key}") private val accessKey: String,
    @Value("\${spring.cloud.aws.credentials.secret-key}") private val secretKey: String
) {

    @Bean
    fun awsCredentialsProvider(): AwsCredentialsProvider {
        return StaticCredentialsProvider.create(
                AwsBasicCredentials.create(accessKey, secretKey)
        )
    }

    @Bean
    fun s3Presigner(credentialsProvider:AwsCredentialsProvider): S3Presigner {
        return S3Presigner.builder()
                .credentialsProvider(credentialsProvider)
                .region(Region.US_EAST_1)
                .build()
    }

    @Bean
    fun s3Client(credentialsProvider: AwsCredentialsProvider): S3Client {
        return S3Client.builder()
                .credentialsProvider(credentialsProvider)
                .region(Region.US_EAST_1)
                .build()
    }
}