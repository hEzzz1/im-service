package org.team324.tcp.feign;

import feign.Headers;
import feign.RequestLine;
import org.team324.common.ResponseVO;
import org.team324.common.model.message.CheckSendMessageReq;

public interface FeignMessageService {

    @Headers({"Content-Type: application/json",
            "Accept: application/json"})
    @RequestLine("POST /message/checkSend")
    public ResponseVO checkSendMessage(CheckSendMessageReq o);

}
