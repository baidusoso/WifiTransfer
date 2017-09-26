# WIFI传书

参照《多看》的WIFI传书功能
 * 手机端的HttpServer采用开源项目[AndroidAsync](https://github.com/koush/AndroidAsync)实现的。
 * 网页端采用jQuery实现，文件上传采用[Upload5](https://github.com/looptribe/upload5)（HTML5浏览器）和ajaxupload.js(非HTML5浏览器，如IE7/IE8/IE9)

## 手机端截图
<img src="./screenshot/phone1.png" width="30%"/>

<img src="./screenshot/phone2.png" width="30%"/>

<img src="./screenshot/phone3.png" width="30%"/>

## 网页版截图
<img src="./screenshot/web.png"/>
## AndroidAsync HttpServer技术要点
[AndroidAsync](https://github.com/koush/AndroidAsync)针对HTTP Servers模块的文档介绍非常少，需要大家自己去看源码。本项目中HTTP server功能都在[WebService.java](./app/src/main/java/com/baidusoso/wifitransfer/WebService.java)这个文件中。下面简单说一下本项目中用到一些传输API:

 * 获取x-www-form-urlencoded请求体
 
```java
UrlEncodedFormBody body = (UrlEncodedFormBody) request.getBody();
``` 
 * 获取multipart/form-data请求体
 
```java
MultipartFormDataBody body = (MultipartFormDataBody) request.getBody()
``` 

 * 发送文本
 
```java
response.send("Hello world!");
```

 * 发送文件流
 
```java
BufferedInputStream bInputStream = ...
response.sendStream(bInputStream, bInputStream.available());
response.sendFile(...);
```

 * 发送Json
 
```java
response.send(new JSONObject());
```

 * 发送Header
 
```java
response.getHeaders().add("Content-Disposition", "attachment;filename=" + URLEncoder.encode(file.getName(), "utf-8"));
```

 * 发送特定的响应码
 
```java
response.code(500);//这个一定要和send()或者end()一起使用
```

## WIFI传输 HttpServer文件上传功能实现

```java
 server.post("/files", (AsyncHttpServerRequest request, AsyncHttpServerResponse response) -> {
                    final MultipartFormDataBody body = (MultipartFormDataBody) request.getBody();
                    body.setMultipartCallback((Part part) -> {
                        if (part.isFile()) {
                            body.setDataCallback((DataEmitter emitter, ByteBufferList bb) -> {
                                fileUploadHolder.write(bb.getAllByteArray());
                                bb.recycle();
                            });
                        } else {
                            if (body.getDataCallback() == null) {
                                body.setDataCallback((DataEmitter emitter, ByteBufferList bb) -> {
                                    try {
                                        String fileName = URLDecoder.decode(new String(bb.getAllByteArray()), "UTF-8");
                                        fileUploadHolder.setFileName(fileName);
                                    } catch (UnsupportedEncodingException e) {
                                        e.printStackTrace();
                                    }
                                    bb.recycle();
                                });
                            }
                        }
                    });
                    request.setEndCallback((Exception e) -> {
                        fileUploadHolder.reset();
                        response.end();
                        RxBus.get().post(Constants.RxBusEventType.LOAD_BOOK_LIST, 0);
                    });
                }
        );
```

 * 这里上传有2个Part：一个文件名，另一个是文件内容。之所以用有文件名Part是为了支持中文文件名：在multipart/form-data上传文件所存的中文文件名会因为AndroidAsync采用ASCII编码导致乱码。因此新增一个文件名Part，内容是网页端JS对文件名进行encodeURL，Server端拿到后再URLDecoder.decode得到中文文件名。
 
 * 这里涉及3个Callback:MultipartCallback、DataCallback和CompletedCallback。AndroidAsync采用NIO，处理请求体时候：每处理一个新的Part会回调MultipartCallback；通过多次回调DataCallback来接收文件内容；处理完所有的请求体回调CompletedCallback表示请求数据都已接收完毕，可以向客户端发送响应，如response.end()结束请求。
 
## TODO
因AndroidAsync采用NIO，目前还不知道怎么获取上传文件的大小。在使用非HTML5浏览器（如IE7/IE8/IE9）时候，文件上传进度无法正确显示。

## 版权声明 
本项目参考《多看》实现的，网页端的资源于《多看》并稍作修改，不得用于任何商业用途，仅供学习研究使用，否则后果自负。
