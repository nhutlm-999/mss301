package vn.edu.fpt.customerservice.service.impl;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.fpt.customerservice.dto.request.CustomerRequest;
import vn.edu.fpt.customerservice.dto.response.CustomerResponse;
import vn.edu.fpt.customerservice.dto.response.EmployeeResponse;
import vn.edu.fpt.customerservice.entity.Customer;
import vn.edu.fpt.customerservice.entity.Employee;
import vn.edu.fpt.customerservice.exception.ResourceNotFoundException;
import vn.edu.fpt.customerservice.repository.CustomerRepository;
import vn.edu.fpt.customerservice.repository.EmployeeRepository;
import vn.edu.fpt.customerservice.service.CustomerService;

@Service
@RequiredArgsConstructor
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;
    private final EmployeeRepository employeeRepository;

    @Override
    @Transactional(readOnly = true)
    public List<CustomerResponse> findAll() {
        return customerRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public CustomerResponse findById(Integer id) {
        return toResponse(findEntity(id));
    }

    @Override
    @Transactional
    public CustomerResponse create(CustomerRequest request) {
        return toResponse(customerRepository.save(toEntity(request, new Customer())));
    }

    @Override
    @Transactional
    public CustomerResponse update(Integer id, CustomerRequest request) {
        return toResponse(customerRepository.save(toEntity(request, findEntity(id))));
    }

    @Override
    @Transactional
    public void delete(Integer id) {
        customerRepository.delete(findEntity(id));
    }

    private Customer findEntity(Integer id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", id));
    }

    private Customer toEntity(CustomerRequest request, Customer customer) {
        customer.setFirstName(request.getFirstName());
        customer.setLastName(request.getLastName());
        customer.setCompany(request.getCompany());
        customer.setAddress(request.getAddress());
        customer.setCity(request.getCity());
        customer.setState(request.getState());
        customer.setCountry(request.getCountry());
        customer.setPostalCode(request.getPostalCode());
        customer.setPhone(request.getPhone());
        customer.setFax(request.getFax());
        customer.setEmail(request.getEmail());

        if (request.getSupportRepId() != null) {
            Employee supportRep = employeeRepository.findById(request.getSupportRepId())
                    .orElseThrow(() -> new ResourceNotFoundException("Employee", request.getSupportRepId()));
            customer.setSupportRep(supportRep);
        } else {
            customer.setSupportRep(null);
        }

        return customer;
    }

    private CustomerResponse toResponse(Customer customer) {
        Employee supportRep = customer.getSupportRep();
        EmployeeResponse employeeResponse = null;

        if (supportRep != null) {
            employeeResponse = EmployeeResponse.builder()
                    .employeeId(supportRep.getEmployeeId())
                    .lastName(supportRep.getLastName())
                    .firstName(supportRep.getFirstName())
                    .title(supportRep.getTitle())
                    .reportsTo(supportRep.getReportsTo() != null ? supportRep.getReportsTo().getEmployeeId() : null)
                    .birthDate(supportRep.getBirthDate())
                    .hireDate(supportRep.getHireDate())
                    .address(supportRep.getAddress())
                    .city(supportRep.getCity())
                    .state(supportRep.getState())
                    .country(supportRep.getCountry())
                    .postalCode(supportRep.getPostalCode())
                    .phone(supportRep.getPhone())
                    .fax(supportRep.getFax())
                    .email(supportRep.getEmail())
                    .build();
        }

        return CustomerResponse.builder()
                .customerId(customer.getCustomerId())
                .firstName(customer.getFirstName())
                .lastName(customer.getLastName())
                .company(customer.getCompany())
                .address(customer.getAddress())
                .city(customer.getCity())
                .state(customer.getState())
                .country(customer.getCountry())
                .postalCode(customer.getPostalCode())
                .phone(customer.getPhone())
                .fax(customer.getFax())
                .email(customer.getEmail())
                .supportRep(employeeResponse)
                .build();
    }
}
