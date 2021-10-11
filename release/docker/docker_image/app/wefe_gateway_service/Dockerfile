FROM wefe_java_base

WORKDIR /opt/gateway

COPY gateway.jar /opt/gateway/gateway.jar
COPY config.properties /opt/gateway/config.properties
COPY start.sh /opt/gateway/start.sh

# RUN chmod +x /opt/gateway/start.sh

CMD ["sh", "/opt/gateway/start.sh"]
