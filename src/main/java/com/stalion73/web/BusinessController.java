package com.stalion73.web;

import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collection;
import java.util.Set;
import java.util.Optional;

import javax.validation.Valid;

import com.stalion73.service.BusinessService;
import com.stalion73.service.SupplierService;
import com.stalion73.model.Business;
import com.stalion73.model.Servise;
import com.stalion73.model.Option;
import com.stalion73.model.Supplier;
import com.stalion73.model.BusinessType;

import org.springframework.http.HttpHeaders;
import org.springframework.hateoas.mediatype.problem.Problem;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

@RestController
@RequestMapping("/business")
public class BusinessController {

    @Autowired
    private final BusinessService businessService;

    @Autowired
    private final SupplierService supplierService;

    private final static HttpHeaders headers = new HttpHeaders();

    public static void setup() {
        headers.setAccessControlAllowOrigin("*");
    }

    public BusinessController(BusinessService businessService, SupplierService supplierService) {
        this.businessService = businessService;
        this.supplierService = supplierService;
    }

    @RequestMapping(value = "", method = RequestMethod.GET, produces = "application/json")
    public ResponseEntity<?> all() {
        BusinessController.setup();
        Collection<Business> businesses = this.businessService.findAll();
        if (businesses.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).headers(headers).body(businesses);
        } else {
            return ResponseEntity.status(HttpStatus.OK).headers(headers).body(businesses);
        }
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.GET, produces = "application/json")
    public ResponseEntity<?> one(@PathVariable("id") Integer id) {
        BusinessController.setup();
        Optional<Business> business = this.businessService.findById(id);
        if (!business.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .header(HttpHeaders.CONTENT_TYPE, MediaTypes.HTTP_PROBLEM_DETAILS_JSON_VALUE).headers(headers)
                    .body(Problem.create().withTitle("Ineffected ID").withDetail("The provided ID doesn't exist"));
        } else {
            return ResponseEntity.status(HttpStatus.OK).headers(headers).body(business.get());
        }
    }

    @RequestMapping(value = "", method = RequestMethod.POST, produces = "application/json")
    public ResponseEntity<?> create(@Valid @RequestBody Business business, BindingResult bindingResult,
            UriComponentsBuilder ucBuilder) {
        BusinessController.setup();
        BindingErrorsResponse errors = new BindingErrorsResponse();
        if (bindingResult.hasErrors() || (business == null)) {
            errors.addAllErrors(bindingResult);
            headers.add("errors", errors.toJSON());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).headers(headers).body(Problem.create()
                    .withTitle("Validation error").withDetail("The provided consumer was not successfuly validated"));
        } else {
            this.businessService.save(business);
            headers.setLocation(ucBuilder.path("/business").buildAndExpand(business.getId()).toUri());
            return ResponseEntity.status(HttpStatus.CREATED).headers(headers).body(business);
        }
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.PUT, produces = "application/json")
    public ResponseEntity<?> update(@PathVariable("id") Integer id, @RequestBody @Valid Business newBusiness,
            BindingResult bindingResult) {
        BusinessController.setup();
        BindingErrorsResponse errors = new BindingErrorsResponse();
        if (bindingResult.hasErrors() || (newBusiness == null)) {
            errors.addAllErrors(bindingResult);
            headers.add("errors", errors.toJSON());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).headers(headers).body(Problem.create()
                    .withTitle("Validation error").withDetail("The provided consumer was not successfuly validated"));
        } else if (!this.businessService.findById(id).isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .header(HttpHeaders.CONTENT_TYPE, MediaTypes.HTTP_PROBLEM_DETAILS_JSON_VALUE).headers(headers)
                    .body(Problem.create().withTitle("Ineffected ID").withDetail("The provided ID doesn't exist"));
        } else {
            // business(name, address, businessType, automatedAccept, Supplier, Servises)
            Business updatedBusiness = this.businessService.findById(id).map(business -> {
                this.businessService.update(id, newBusiness);
                Supplier supplier;
                if (newBusiness.getSupplier() == null) {
                    supplier = business.getSupplier();
                } else {
                    this.supplierService.update(business.getId(), newBusiness.getSupplier());
                    supplier = this.supplierService.findById(business.getId()).get();
                }
                business.setSupplier(supplier);
                this.businessService.save(business);
                return business;
            }).orElseGet(() -> {
                return null;
            });
            return new ResponseEntity<Business>(updatedBusiness, headers, HttpStatus.NO_CONTENT);
        }
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE, produces = "application/json")
    public ResponseEntity<?> delete(@PathVariable("id") Integer id) {
        BusinessController.setup();
        Optional<Business> business = this.businessService.findById(id);
        if (business.isPresent()) {
            this.businessService.deleteById(id);
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .header(HttpHeaders.CONTENT_TYPE, MediaTypes.HTTP_PROBLEM_DETAILS_JSON_VALUE).headers(headers)
                    .body(Problem.create().withTitle("Ineffected ID").withDetail("The provided ID doesn't exist"));
        }
    }

}
