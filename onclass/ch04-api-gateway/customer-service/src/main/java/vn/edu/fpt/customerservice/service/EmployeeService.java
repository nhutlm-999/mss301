package vn.edu.fpt.customerservice.service;

import java.util.List;
import vn.edu.fpt.customerservice.dto.request.EmployeeRequest;
import vn.edu.fpt.customerservice.dto.response.EmployeeResponse;

public interface EmployeeService {

    List<EmployeeResponse> findAll();

    EmployeeResponse findById(Integer id);

    EmployeeResponse create(EmployeeRequest request);

    EmployeeResponse update(Integer id, EmployeeRequest request);

    void delete(Integer id);
}
