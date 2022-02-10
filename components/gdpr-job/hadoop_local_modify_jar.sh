zip -d target/gdpr-job-1.0-SNAPSHOT.jar META-INF/LICENSE
zip -d target/gdpr-job-1.0-SNAPSHOT.jar LICENSE

jar -tvf target/gdpr-job-1.0-SNAPSHOT.jar |grep META-INF/LICENSE