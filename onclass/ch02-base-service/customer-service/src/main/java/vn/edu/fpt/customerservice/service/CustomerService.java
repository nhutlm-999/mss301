package vn.edu.fpt.customerservice.service;

import java.util.List;
import vn.edu.fpt.customerservice.dto.request.CustomerRequest;
import vn.edu.fpt.customerservice.dto.response.CustomerResponse;

public interface CustomerService {

    List<CustomerResponse> findAll();

    CustomerResponse findById(Integer id);

    CustomerResponse create(CustomerRequest request);

    CustomerResponse update(Integer id, CustomerRequest request);

    void delete(Integer id);
}
