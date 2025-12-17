package contracts.buyer

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description "查询会员预存款余额"
    
    request {
        method GET()
        url "/buyer/wallet/wallet"
        headers {
            header('Authorization', 'Bearer mock-token')
        }
    }
    
    response {
        status 200
        headers {
            contentType(applicationJson())
        }
        body([
            success: true,
            code: 200,
            message: "SUCCESS",
            result: [
                id: "1",
                memberId: "1",
                memberName: $(consumer("张三"), producer(regex('[\\S\\s]+'))),
                balance: $(consumer(1000.00), producer(regex('[0-9]+\\.?[0-9]*'))),
                frozenPrice: $(consumer(0.00), producer(regex('[0-9]+\\.?[0-9]*'))),
                walletPassword: null
            ]
        ])
    }
}
