package com.leyou.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public enum ExceptionEnum {
    CATEGORY_NOT_FOUND(404,"商品分类没查到"),
    BRAND_NOT_FOUND(404,"品牌不存在"),
    BRAND_SAVE_ERROR(500,"保存品牌发生错误"),
    UPLOAD_FILE_ERROR(500,"文件上传失败"),
    INVALID_FILE_TYPE(400,"无效的文件类型"),
    UPDATE_BRAND_ERROR(500,"更新品牌信息失败"),
    DELETE_BRAND_ERROR(500,"删除品牌失败"),
    GOODS_NOT_FOUND(404,"商品不存在"),
    GOODS_SAVE_ERROR(500,"新增商品失败"),
    GOODS_EDIT_ERROR(500,"保存商品失败"),
    GOODS_DELETE_ERROR(500,"删除商品失败"),
    SKU_NOT_FOUND(500,"sku查询失败"),
    SPEC_GROUP_NOT_FOUND(500,"规格参数分组未找到"),
    SPEC_GROUP_CREATE_FAILED(500,"规格参数分组创建失败"),
    INVALID_PARAM(400, "参数错误"),
    DELETE_SPEC_GROUP_FAILED(500, "商品规格组删除失败"),
    UPDATE_SPEC_GROUP_FAILED(500, "商品规格组更新失败"),
    SPEC_PARAM_NOT_FOUND(204, "规格参数查询失败"),
    UPDATE_SPEC_PARAM_FAILED(500, "商品规格参数更新失败"),
    DELETE_SPEC_PARAM_FAILED(500, "商品规格参数删除失败"),
    SPEC_PARAM_CREATE_FAILED(500, "新增规格参数失败"),
    STOCK_NOT_FOUND(204, "库存查询失败"),
    SPU_NOT_FOUND(201, "SPU未查询到"),
    GOODS_UPDATE_ERROR(500, "商品更新失败"),
    DELETE_GOODS_ERROR(500, "删除商品错误"),
    UPDATE_SALEABLE_ERROR(500, "更新商品销售状态错误"),
    STOCK_NOT_ENOUGH(500, "商品库存不足"),
    GOODS_NOT_SALEABLE(404,"商品未上架"),
    USER_DATA_TYPE_ERROR(400,"用户数据类型不匹配"),
    INVALID_VERIFY_CODE(400,"无效的验证码"),
    INVALID_USERNAME_PASSWORD(400,"无效的用户名或者密码"),
    USERNAME_OR_PASSWORD_ERROR(400,"用户名或密码错误"),
    CREATE_TOKEN_ERROR(400,"创建token失败"),
    UNAUTHORIZED(403,"未授权"),
    CART_NOT_FOUND(404,"商品为空"),
    RECEIVER_ADDRESS_NOT_FOUND(404,"收货人地址查询失败"),
    ORDER_NOT_FOUND(404,"订单没有找到"),
    ORDER_STATUS_EXCEPTION(404,"订单状态异常"),
    CREATE_PAY_URL_ERROR(404,"创建支付失败"),
    WX_PAY_SIGN_INVALID(400, "微信支付签名异常"),
    WX_PAY_NOTIFY_PARAM_ERROR(400, "微信支付回调参数异常"),
    ;
    private int code;
    private String msg;

}
