def call(String namespace, String service,String imageUrl){
        withEnv([
           "NAMESPACE=${namespace}",
           "SERVICE=${service}",
           "IMAGE_URL=${imageUrl}"
         ]){
      sh '''
         TAG=`echo "$IMAGE_URL" |awk -F':' '{ print \$NF}'`
         NEW_IMAGE_URL=docker.byd.com/${NAMESPACE}/${SERVICE}:$TAG
         docker pull $IMAGE_URL
         docker tag $IMAGE_URL $NEW_IMAGE_URL
         docker push $NEW_IMAGE_URL
         echo "$NEW_IMAGE_URL" > ./image_url.output
      '''
       def currentImage = sh(script: "cat ./image_url.output", returnStdout: true).trim()
       stash name: 'image-url', includes: 'image_url.output'
       return currentImage
      }
}
