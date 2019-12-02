# crawler-adminapp

## Project setup
```
npm install
```

### Compiles and hot-reloads for development
```
npm run serve
```

### Compiles and minifies for production
```
npm run build
```

### Lints and fixes files
```
npm run lint
```

### Customize configuration
See [Configuration Reference](https://cli.vuejs.org/config/).

#### Deploying to AWS

### Build the image

    docker build -t azh4r/productsearch-app-admin:<tag_version_number> .

### Run the image locally to test it
    docker run -i -t -p 8080:8080 azh4r/productsearch-app-admin:1.0.3

### Push image to docker Hub

    docker push azh4r/productsearch-app-admin:1.0.3

### deploy the new image to AWS EKS

## ssh to AWS EKS
   
`cd git/website-crawler/crawler-k8s/manifest/`
create the admin-service.yaml if it doesn't already exist.

create the admin-deployment.yaml file if it doesn't exist.

change the image name in the admin-deployment.yaml to the version that is to be deployed and was pushed out to docker-hub

Run the latest front end deployment by applying the admin-deployment.yaml
`kubectl apply -f admin-deployment.yaml` 






