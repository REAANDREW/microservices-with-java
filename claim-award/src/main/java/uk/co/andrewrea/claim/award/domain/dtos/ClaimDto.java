package uk.co.andrewrea.claim.award.domain.dtos;

import uk.co.andrewrea.claim.award.domain.dtos.AddressDto;
import uk.co.andrewrea.claim.award.domain.dtos.BankAccountDto;

/**
 * Created by vagrant on 5/8/16.
 */
public class ClaimDto {
    public String firstname;
    public String surname;
    public String middlenames;
    public String dob;
    public Integer income;
    public String passportNumber;
    public AddressDto address;
    public BankAccountDto bankAccount;
    public String email;
    public Boolean receiveEmail;
}
