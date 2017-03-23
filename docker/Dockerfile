# Run like this:
# sudo docker run -t -i --rm -p 8443:8443 <image-id>
# 
# at deployment time the spring boot configuration properties are overridden by environment variable provided by the AWS docker service, check
# https://docs.spring.io/spring-boot/docs/current/reference/html/boot-features-external-config.html for more information on spring boot configuration
#
FROM java:8

RUN wget "https://dma.ci.cloudbees.com/job/NoGoService/lastSuccessfulBuild/artifact/build/libs/NoGoService.war"

EXPOSE 8443

CMD ["java",  "-jar", "nogoservice.war"]
