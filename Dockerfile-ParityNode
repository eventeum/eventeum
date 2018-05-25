FROM parity/parity:v1.9.5
EXPOSE 8545 8180
#Install node
RUN apt-get update
RUN apt-get -y install git
RUN apt-get -y install build-essential
RUN apt-get -y install curl
RUN curl -sL https://deb.nodesource.com/setup_9.x | bash -
RUN apt-get -y install nodejs
RUN sudo npm install -g web3 --unsafe-perm
ENV NODE_PATH /usr/lib/node_modules
ENTRYPOINT ["sh", "run-parity.sh"]
ADD docker-scripts/run-parity.sh .