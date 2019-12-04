FROM node:10.15.2
WORKDIR /myapp
COPY package*.json ./
RUN npm install 
COPY . .
RUN npm run build
EXPOSE 8080
CMD ["npm", "run", "serve-production"]
