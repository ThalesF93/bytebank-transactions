//package br.com.bytebank.transactions.openfeign.scheduler;
//
//
//import br.com.bytebank.customers.entity.PendingAccountOpening;
//import br.com.bytebank.customers.exception.AccountNotCreatedException;
//import br.com.bytebank.customers.openfeign.dtos.requests.AccountRequestDTO;
//import br.com.bytebank.customers.openfeign.feignclient.AccountClient;
//import br.com.bytebank.customers.repositories.PendingAccountRepository;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.scheduling.annotation.Scheduled;
//import org.springframework.stereotype.Component;
//
//import java.util.List;
//
//@Component
//public class AccountRetryScheduler {
//
//    @Autowired
//    private PendingAccountRepository repository;
//
//    @Autowired
//    private AccountClient accountClient;
//
//    @Scheduled(fixedDelay = 60000)
//    public  void retryPendingAccounts(){
//
//        List<PendingAccountOpening> list = repository.findByProcessedFalse();
//
//        for (PendingAccountOpening pendingOpening : list){
//
//            try {
//                AccountRequestDTO requestDTO = new AccountRequestDTO(pendingOpening.getId());
//                accountClient.openAccount(requestDTO);
//
//                repository.delete(pendingOpening);
//            } catch (AccountNotCreatedException e) {
//                pendingOpening.setAttempts(pendingOpening.getAttempts()+1);
//                repository.save(pendingOpening);
//            }
//        }
//    }
//
//}
