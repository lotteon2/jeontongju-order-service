package com.jeontongju.order.controller;

import com.jeontongju.order.dto.DeliveryDto;
import com.jeontongju.order.dto.OrderCancelRequestDto;
import com.jeontongju.order.dto.ProductOrderCancelRequestDto;
import com.jeontongju.order.dto.response.admin.SettlementForAdmin;
import com.jeontongju.order.dto.response.consumer.ConsumerOrderListResponseDto;
import com.jeontongju.order.dto.response.consumer.ConsumerOrderListResponseDtoForAdmin;
import com.jeontongju.order.dto.response.consumer.ProductOrderConfirmResponseDto;
import com.jeontongju.order.dto.response.seller.SellerOrderListResponseDto;
import com.jeontongju.order.dto.response.seller.SettlementForSeller;
import com.jeontongju.order.exception.InvalidPermissionException;
import com.jeontongju.order.service.OrderService;
import io.github.bitbox.bitbox.dto.ResponseFormat;
import io.github.bitbox.bitbox.enums.MemberRoleEnum;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class OrderController {
    private final OrderService orderService;

    @GetMapping("/order/consumer")
    public ResponseEntity<ResponseFormat<ConsumerOrderListResponseDto>> getConsumerOrderList(
            @PageableDefault(sort = "orderDate", direction = Sort.Direction.DESC)Pageable pageable,
            @RequestHeader Long memberId, @RequestHeader MemberRoleEnum memberRole, @RequestParam(required = false) Boolean isAuction){
        checkMemberRole(memberRole, MemberRoleEnum.ROLE_CONSUMER);
        return ResponseEntity.ok().body(ResponseFormat.<ConsumerOrderListResponseDto>builder()
                .code(HttpStatus.OK.value())
                .message(HttpStatus.OK.getReasonPhrase())
                .detail("주문 내역 조회 성공")
                .data(orderService.getConsumerOrderList(memberId, isAuction, pageable))
        .build());
    }

    @GetMapping("/order/consumer/{consumerId}")
    public ResponseEntity<ResponseFormat<ConsumerOrderListResponseDtoForAdmin>> getConsumerOrderListForAdmin(
            @RequestHeader MemberRoleEnum memberRole,
            @PathVariable Long consumerId, @PageableDefault(sort = "orderDate", direction = Sort.Direction.DESC)Pageable pageable){
        checkMemberRole(memberRole, MemberRoleEnum.ROLE_ADMIN);
        return ResponseEntity.ok().body(ResponseFormat.<ConsumerOrderListResponseDtoForAdmin>builder()
                .code(HttpStatus.OK.value())
                .message(HttpStatus.OK.getReasonPhrase())
                .detail("주문내역 조회 완료")
                .data(orderService.getConsumerOrderList(consumerId, pageable))
        .build());
    }

    @GetMapping("/order/seller")
    public ResponseEntity<ResponseFormat<SellerOrderListResponseDto>> getSellerOrderList(
            @PageableDefault(sort = "orderDate", direction = Sort.Direction.DESC)Pageable pageable,
            @RequestHeader MemberRoleEnum memberRole,
            @RequestHeader Long memberId, @RequestParam String orderDate, @RequestParam String productId, @RequestParam boolean isDeliveryCodeNull){
        checkMemberRole(memberRole, MemberRoleEnum.ROLE_SELLER);
        return ResponseEntity.ok().body(ResponseFormat.<SellerOrderListResponseDto>builder()
                .code(HttpStatus.OK.value())
                .message(HttpStatus.OK.getReasonPhrase())
                .detail("주문내역 조회 완료")
                .data(orderService.getSellerOrderList(memberId, orderDate, productId, isDeliveryCodeNull, pageable))
        .build());
    }

    @GetMapping("/order/seller/{sellerId}")
    public ResponseEntity<ResponseFormat<SellerOrderListResponseDto>> getSellerOrderListForAdmin(
            @PathVariable Long sellerId, @PageableDefault(sort = "orderDate", direction = Sort.Direction.DESC)Pageable pageable,
            @RequestHeader MemberRoleEnum memberRole, @RequestParam String orderDate, @RequestParam String productId){
        checkMemberRole(memberRole, MemberRoleEnum.ROLE_ADMIN);
        return ResponseEntity.ok().body(ResponseFormat.<SellerOrderListResponseDto>builder()
                .code(HttpStatus.OK.value())
                .message(HttpStatus.OK.getReasonPhrase())
                .detail("주문내역 조회 완료")
                .data(orderService.getSellerOrderList(sellerId, orderDate, productId, false, pageable))
        .build());
    }

    @GetMapping("/settlement/seller/{sellerId}")
    public ResponseEntity<ResponseFormat<List<SettlementForAdmin>>> getSettlementForAdmin(@PathVariable Long sellerId, @RequestParam Long year,
                                                                                         @RequestHeader MemberRoleEnum memberRole){
        checkMemberRole(memberRole, MemberRoleEnum.ROLE_ADMIN);
        return ResponseEntity.ok().body(ResponseFormat.<List<SettlementForAdmin>>builder()
                .code(HttpStatus.OK.value())
                .message(HttpStatus.OK.getReasonPhrase())
                .detail("특정 셀러 정산 내역 조회 완료")
                .data(orderService.getSettlementForAdmin(sellerId,year))
        .build());
    }

    // 내 정산 내역 조회(셀러)
    @GetMapping("/settlement/seller/year/{year}/month/{month}")
    public ResponseEntity<ResponseFormat<SettlementForSeller>> getSettlementForSeller(@PathVariable Long year, @PathVariable Long month,
                                                                                      @RequestHeader Long memberId, @RequestHeader MemberRoleEnum memberRole){
        checkMemberRole(memberRole, MemberRoleEnum.ROLE_SELLER);
        return ResponseEntity.ok().body(ResponseFormat.<SettlementForSeller>builder()
                .code(HttpStatus.OK.value())
                .message(HttpStatus.OK.getReasonPhrase())
                .detail("나의 정산 내역 조회 완료")
                .data(orderService.getSettlementForSeller(memberId,year,month))
        .build());
    }

    @PatchMapping("/delivery/{deliveryId}")
    public ResponseEntity<ResponseFormat<Void>> addDeliveryCode(@PathVariable long deliveryId, @RequestHeader MemberRoleEnum memberRole, @Valid @RequestBody DeliveryDto deliveryDto){
        checkMemberRole(memberRole, MemberRoleEnum.ROLE_SELLER);
        orderService.addDeliveryCode(deliveryId,deliveryDto.getDeliveryCode());
        return ResponseEntity.ok().body(ResponseFormat.<Void>builder()
                .code(HttpStatus.OK.value())
                .message(HttpStatus.OK.getReasonPhrase())
                .detail("운송장 등록완료")
        .build());
    }

    @PatchMapping("/delivery-confirm/{deliveryId}")
    public ResponseEntity<ResponseFormat<Void>> confirmDelivery(@PathVariable Long deliveryId, @RequestHeader MemberRoleEnum memberRole){
        checkMemberRole(memberRole, MemberRoleEnum.ROLE_SELLER);
        orderService.confirmDelivery(deliveryId);
        return ResponseEntity.ok().body(ResponseFormat.<Void>builder()
                .code(HttpStatus.OK.value())
                .message(HttpStatus.OK.getReasonPhrase())
                .detail("배송 완료 상태 변경 완료")
        .build());
    }

    @PatchMapping("/product-order-confirm/{productOrderId}")
    public ResponseEntity<ResponseFormat<ProductOrderConfirmResponseDto>> confirmProductOrder(@PathVariable Long productOrderId, @RequestHeader MemberRoleEnum memberRole){
        checkMemberRole(memberRole, MemberRoleEnum.ROLE_CONSUMER);
        return ResponseEntity.ok().body(ResponseFormat.<ProductOrderConfirmResponseDto>builder()
                .code(org.springframework.http.HttpStatus.OK.value())
                .message(HttpStatus.OK.getReasonPhrase())
                .detail("주문 확정 완료")
                .data(
                        ProductOrderConfirmResponseDto.builder()
                                .point(orderService.confirmProductOrder(productOrderId))
                        .build()
                )
        .build());
    }

    @PostMapping("/order-cancel")
    public ResponseEntity<ResponseFormat<Void>> cancelProductOrder(@Valid @RequestBody OrderCancelRequestDto orderCancelRequestDto, @RequestHeader MemberRoleEnum memberRole){
        checkMemberRole(memberRole, MemberRoleEnum.ROLE_CONSUMER);
        orderService.cancelOrder(orderCancelRequestDto.getOrdersId());

        return ResponseEntity.ok().body(ResponseFormat.<Void>builder()
                .code(HttpStatus.OK.value())
                .message(HttpStatus.OK.getReasonPhrase())
                .detail("주문 취소 완료")
        .build());
    }

    @PostMapping("/product-order-cancel")
    public ResponseEntity<ResponseFormat<Void>> cancelOrder(@Valid @RequestBody ProductOrderCancelRequestDto productOrderCancelRequestDto, @RequestHeader MemberRoleEnum memberRole){
        checkMemberRole(memberRole, MemberRoleEnum.ROLE_CONSUMER);
        orderService.cancelProductOrder(productOrderCancelRequestDto.getProductOrderId());

        return ResponseEntity.ok().body(ResponseFormat.<Void>builder()
                .code(HttpStatus.OK.value())
                .message(HttpStatus.OK.getReasonPhrase())
                .detail("상품주문 취소 완료")
        .build());
    }

    private void checkMemberRole(MemberRoleEnum currentRole, MemberRoleEnum targetRole) {
        if(currentRole != targetRole){
            throw new InvalidPermissionException("권한이 부족 합니다.");
        }
    }
}