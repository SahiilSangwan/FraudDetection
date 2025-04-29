package com.secure.utils;

import com.secure.model.Account;
import com.secure.model.Beneficiary;
import com.secure.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import com.secure.model.Transaction;

@Service
public class TemplateProvider {

    private final TemplateEngine templateEngine;

    public TemplateProvider(TemplateEngine templateEngine) {
        this.templateEngine = templateEngine;
    }

    public String buildOtpEmailContent(String otp) {
        Context context = new Context();
        context.setVariable("otp", otp);

        return templateEngine.process("otp-email", context);
    }

    public String buildLoginSuccessEmailContent() {
        Context context = new Context();
        // You can add more dynamic content using context.setVariable("key", value);
        return templateEngine.process("login-success", context);
    }

    public String buildMultipleAttemptWarningEmail(String timestamp) {
        Context context = new Context();
        context.setVariable("timestamp", timestamp);
        return templateEngine.process("multiple-attempts", context);
    }

    public String buildAccountSuspendedEmail(String reason, String timestamp) {
        Context context = new Context();
        context.setVariable("reason", reason);
        context.setVariable("timestamp", timestamp);
        return templateEngine.process("account-suspended", context);
    }

    public String buildAdminLoginSuccessEmail(String loginTime) {
        Context context = new Context();
        context.setVariable("loginTime", loginTime);
        return templateEngine.process("admin-login-success", context);
    }

    public String buildMpinUpdateConfirmationEmail(String changedOn) {
        Context context = new Context();
        context.setVariable("changedOn", changedOn);
        return templateEngine.process("mpin-update-confirmation", context);
    }

    public String buildAccountRestoredEmail(String restoredAt) {
        Context context = new Context();
        context.setVariable("restoredAt", restoredAt);
        return templateEngine.process("account-restored", context);
    }

    public String buildBeneficiaryAddedEmail(Beneficiary beneficiary, String coolingEndTime) {
        Context context = new Context();
        context.setVariable("beneficiaryName", beneficiary.getBeneficiaryName());
        context.setVariable("accountNumber", beneficiary.getBeneficiaryAccountNumber());
        context.setVariable("ifscCode", beneficiary.getIfscCode());
        context.setVariable("amount", beneficiary.getAmount());
        context.setVariable("coolingEndTime", coolingEndTime);
        return templateEngine.process("beneficiary-added", context);
    }

    public String buildTransferLimitUpdatedEmail(Beneficiary beneficiary, String coolingEndTime) {
        Context context = new Context();
        context.setVariable("beneficiaryName", beneficiary.getBeneficiaryName());
        context.setVariable("accountNumber", beneficiary.getBeneficiaryAccountNumber());
        context.setVariable("ifscCode", beneficiary.getIfscCode());
        context.setVariable("newAmount", beneficiary.getAmount());
        context.setVariable("coolingEndTime", coolingEndTime);
        return templateEngine.process("transfer-limit-updated", context);
    }

    public String buildDebitNotificationEmail(Transaction transaction, Account sender,Account receiver, String formattedDateTime) {
        Context context = new Context();
        context.setVariable("amount", transaction.getAmountTransferred());
        context.setVariable("dateTime", formattedDateTime);
        context.setVariable("balance", sender.getBalance());
        context.setVariable("transactionId", transaction.getTransactionId());
        context.setVariable("recipientAccount", receiver.getAccountNumber());
        return templateEngine.process("account-debit-notification", context);
    }

    public String buildCreditNotificationEmail(Transaction transaction, Account receiver, User sender, String formattedDateTime, String description) {
        Context context = new Context();
        context.setVariable("amount", transaction.getAmountTransferred());
        context.setVariable("dateTime", formattedDateTime);
        context.setVariable("balance", receiver.getBalance());
        context.setVariable("transactionId", transaction.getTransactionId());
        context.setVariable("senderName", sender.getFirstName() + " " + sender.getLastName());
        context.setVariable("description", description);
        return templateEngine.process("payment-received-notification", context);
    }







}
