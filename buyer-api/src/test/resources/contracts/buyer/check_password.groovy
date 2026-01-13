package contracts.buyer

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description "检测会员是否设置过支付密码"
    
    request {
        method GET()
        url "/buyer/wallet/wallet/check"
        headers {
            header('Authorization', 'Bearer mock-token')
        }
    }
    
    response {
        status 200
        headers {
            contentType(applicationJson())
        }
        body($(consumer(true), producer(anyBoolean())))
    }
}
