# Extend vert.x image
FROM vertx/vertx3

ENV VERTICLE_NAME io.fizz.gateway.http.Application
ENV VERTICLE_HOME /usr/verticles

EXPOSE 3000

# Copy files into the container
COPY entrypoint.sh application.jar newrelic $VERTICLE_HOME/

# Update working directory
WORKDIR $VERTICLE_HOME

# Update required permissions
RUN chmod +x ./entrypoint.sh

# Launch the verticle
ENTRYPOINT ["./entrypoint.sh"]
CMD [$VERTICLE_NAME]