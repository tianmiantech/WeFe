FROM wefe_java_base

WORKDIR /opt/service

COPY fusion-service.jar /opt/service/fusion-service.jar

COPY start.sh /opt/service/start.sh

# RUN chmod +x /opt/service/start.sh

CMD ["sh", "/opt/service/start.sh"]
