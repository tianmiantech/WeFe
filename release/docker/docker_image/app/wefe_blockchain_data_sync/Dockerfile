FROM wefe_java_base

WORKDIR /opt/service

COPY wefe-blockchain-data-sync.jar /opt/service/wefe-blockchain-data-sync.jar

COPY start.sh /opt/service/start.sh

# RUN chmod +x /opt/service/start.sh

CMD ["sh", "/opt/service/start.sh"]
