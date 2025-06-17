package com.swp2.demo.Controller;


import com.swp2.demo.Repository.PaymentRepository;
import com.swp2.demo.entity.MomoRequest;
import com.swp2.demo.entity.Payment;
import com.swp2.demo.service.MomoUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Controller
public class PaymentController {

    @Autowired
    private PaymentRepository paymentRepository;

    @Value("${momo.partnerCode}")
    private String partnerCode;

    @Value("${momo.accessKey}")
    private String accessKey;

    @Value("${momo.secretKey}")
    private String secretKey;

    @Value("${momo.endpoint}")
    private String endpoint;

    @Value("${momo.returnUrl}")
    private String returnUrl;

    @Value("${momo.notifyUrl}")
    private String notifyUrl;

    @Value("${momo.partnerName}")
    private String partnerName;

    @Value("${momo.storeId}")
    private String storeId;


    @GetMapping("/")
    public String home() {
        return "payment";
    }

    @RequestMapping("/pay")
    public String payWithMomo(@RequestParam("amount") Long amount, Model model) {
        try {
            MomoRequest request = MomoUtil.createRequest(
                    partnerCode, accessKey, secretKey, endpoint,
                    returnUrl, notifyUrl, amount, "Thanh toán MoMo",
                    partnerName, storeId
            );

            // Save pending payment info to DB
            Payment payment = new Payment();
            payment.setOrderId(request.getOrderId());
            payment.setRequestId(request.getRequestId());
            payment.setAmount(amount);
            payment.setStatus("pending");
            payment.setMessage("Chờ thanh toán");
            paymentRepository.save(payment);

            // Send request to MoMo
            RestTemplate restTemplate = new RestTemplate();
            Map<String, Object> response = restTemplate.postForObject(endpoint, request, Map.class);

            String payUrl = (String) response.get("payUrl");
            return "redirect:" + payUrl;
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", e.getMessage());
            return "error";
        }
    }


    @GetMapping("/paymentBill")
    public String momoResult(@RequestParam Map<String, String> params, Model model) {
        String resultCode = params.get("resultCode");
        String message = params.get("message");
        String orderId = params.get("orderId");

        Payment payment = paymentRepository.findByOrderId(orderId);
        if (payment != null) {
            if ("0".equals(resultCode)) {
                payment.setStatus("success");
                payment.setMessage("Thanh toán thành công");
                model.addAttribute("status", "success");
                model.addAttribute("message", "Thanh toán thành công!");
            } else {
                payment.setStatus("fail");
                payment.setMessage(message);
                model.addAttribute("status", "fail");
                model.addAttribute("message", "Giao dịch thất bại: " + message);
            }
            paymentRepository.save(payment);
        }
        if (orderId == null || resultCode == null) {
            model.addAttribute("status", "fail");
            model.addAttribute("message", "Thiếu thông tin xác nhận giao dịch!");
            return "result";
        }


        model.addAttribute("paymentParam", params);
        return "paymentBill";
    }
    @RequestMapping("/notify")
    @ResponseBody
    public String notify(@RequestParam Map<String, String> allParams) {
        boolean isValid = new MomoUtil().verifySignature(allParams, secretKey);
        if (!isValid) {
            return "invalid signature";
        }

        String orderId = allParams.get("orderId");
        String resultCode = allParams.get("resultCode");

        Payment payment = paymentRepository.findByOrderId(orderId);
        if (payment != null) {
            if ("0".equals(resultCode)) {
                payment.setStatus("success");
                payment.setMessage("Thanh toán thành công (IPN)");
            } else {
                payment.setStatus("fail");
                payment.setMessage("Thất bại (IPN): " + allParams.get("message"));
            }
            paymentRepository.save(payment);
        }
        return "success";
    }

}