FROM node:18-alpine
WORKDIR /usr/src/app
COPY package.json .
RUN npm install --production
COPY . .
RUN mkdir -p /usr/src/app/uploads
EXPOSE 4000
CMD ["node", "server.js"]

