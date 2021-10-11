FROM wefe_java_base

WORKDIR /opt/service

COPY board-service.jar /opt/service/board-service.jar

COPY start.sh /opt/service/start.sh

# RUN chmod +x /opt/service/start.sh

CMD ["sh", "/opt/service/start.sh"]
