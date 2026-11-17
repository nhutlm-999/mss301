package vn.edu.fpt.customerservice.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Customer representation")
public class CustomerResponse {

    @Schema(example = "1")
    private Integer customerId;

    @Schema(example = "Luís")
    private String firstName;

    @Schema(example = "Gonçalves")
    private String lastName;

    @Schema(example = "Embraer - Empresa Brasileira de Aeronáutica S.A.")
    private String company;

    @Schema(example = "Av. Brigadeiro Faria Lima, 2170")
    private String address;

    @Schema(example = "São José dos Campos")
    private String city;

    @Schema(example = "SP")
    private String state;

    @Schema(example = "Brazil")
    private String country;

    @Schema(example = "12227-000")
    private String postalCode;

    @Schema(example = "+55 (12) 3923-5555")
    private String phone;

    @Schema(example = "+55 (12) 3923-5566")
    private String fax;

    @Schema(example = "luisg@embraer.com.br")
    private String email;

    private EmployeeResponse supportRep;
}
