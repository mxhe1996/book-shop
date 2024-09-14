package com.hmx.shop.utils;

import com.hmx.shop.domain.CommonResponseBody;

public class ResponseUtils {


    public static CommonResponseBody toResponse(Object t){
        return new CommonResponseBody<>(t);
    }

    public static CommonResponseBody toRow(int row){
        return row==0?errorResponse("操作失败"):successResponse();
    }


    public static CommonResponseBody successResponse(){
        CommonResponseBody commonResponseBody = new CommonResponseBody();
        commonResponseBody.setMsg("success");
        commonResponseBody.setCode(200);
        return commonResponseBody;
    }

    public static CommonResponseBody errorResponse(String errorInfo){
        CommonResponseBody commonResponseBody = new CommonResponseBody();
        commonResponseBody.setMsg(errorInfo);
        commonResponseBody.setCode(502);
        return commonResponseBody;
    }

}
