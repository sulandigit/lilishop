package cn.lili.modules.security.entity.vo;

import cn.lili.common.utils.ObjectUtil;
import cn.lili.elasticsearch.EsSuffix;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;

/**
 * 安全审计日志
 *
 * @author Qoder
 * @since 2026-01-05
 */
@Data
@Document(indexName = "#{@elasticsearchProperties.indexPrefix}_" + EsSuffix.SECURITY_LOGS_INDEX_NAME)
@ToString
@NoArgsConstructor
@Accessors(chain = true)
public class SecurityLogVO implements Serializable {

    private static final long serialVersionUID = -8995552592401630087L;

    @Id
    @ApiModelProperty(value = "id")
    private String id;

    @ApiModelProperty(value = "日志记录时间")
    @Field(type = FieldType.Long)
    private Long createTime = new Date().getTime();

    @ApiModelProperty(value = "请求用户")
    @Field(type = FieldType.Text)
    private String username;

    @ApiModelProperty(value = "用户ID")
    @Field(type = FieldType.Keyword)
    private String userId;

    @ApiModelProperty(value = "用户类型")
    @Field(type = FieldType.Keyword)
    private String userType;

    @ApiModelProperty(value = "请求路径")
    @Field(type = FieldType.Text)
    private String requestUrl;

    @ApiModelProperty(value = "请求参数")
    @Field(type = FieldType.Text)
    private String requestParam;

    @ApiModelProperty(value = "ip")
    @Field(type = FieldType.Keyword)
    private String ip;

    @ApiModelProperty(value = "操作类型")
    @Field(type = FieldType.Keyword)
    private String operationType;

    @ApiModelProperty(value = "请求类型")
    @Field(type = FieldType.Keyword)
    private String requestType;

    @ApiModelProperty(value = "安全级别")
    @Field(type = FieldType.Keyword)
    private String securityLevel;

    @ApiModelProperty(value = "操作结果")
    @Field(type = FieldType.Keyword)
    private String result;

    @ApiModelProperty(value = "异常信息")
    @Field(type = FieldType.Text)
    private String exceptionInfo;

    @ApiModelProperty(value = "ip信息")
    @Field(type = FieldType.Text)
    private String ipInfo;

    @ApiModelProperty(value = "花费时间")
    @Field(type = FieldType.Integer)
    private Integer costTime;

    @ApiModelProperty(value = "商家ID")
    @Field(type = FieldType.Long)
    private Long storeId = -1L;

    @ApiModelProperty(value = "资源对象")
    @Field(type = FieldType.Text)
    private String resourceObject;

    @ApiModelProperty(value = "资源ID")
    @Field(type = FieldType.Keyword)
    private String resourceId;

    /**
     * 转换请求参数为Json
     *
     * @param paramMap
     */
    public void setMapToParams(Map<String, String[]> paramMap) {
        this.requestParam = ObjectUtil.mapToString(paramMap);
    }
}