import groovy.json.JsonOutput

def call(String url,String content) {
    def data = [
        msgtype : "markdown",
        markdown  : [
           content: content
        ]
    ]
    def jsonPayload = JsonOutput.toJson(data)
    def response = sh(
                        script: """
                            set +x
                            curl -s -X POST ${url} \
                            -H "Content-Type: application/json" \
                            -d '${jsonPayload}'
                        """,
                        returnStdout: true
                    ).trim()
    echoColor("审批消息发送结果: $response")
//    try{
//        def connection = new URL(url).openConnection() as HttpURLConnection
//        connection.requestMethod = "POST"
//        connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8")
//        connection.setRequestProperty("Accept", "application/json")
//        connection.setDoOutput(true)
//        OutputStream outputStream = connection.getOutputStream()
//        OutputStreamWriter writer = new OutputStreamWriter(outputStream, "UTF-8")
//        writer.write(jsonPayload)
//        writer.flush() // 确保所有数据都写入
//        writer.close() // 关闭输出流
//        int responseCode = connection.getResponseCode()
//        println "Response Code: $responseCode"
//        // 读取响应
//        InputStream inputStream = connection.getInputStream()
//        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"))
//        reader.close() // 关闭输入流
//        connection.disconnect() // 断开连接
//        echoColor("信息发送成功")
//    }catch(Exception e){
//      echoColor("信息发送失败: ${e.message}","red")
//    }
}