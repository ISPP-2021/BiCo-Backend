package com.stalion73.web;


import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Collection;
import java.util.Optional;

import javax.validation.Valid;

import com.stalion73.service.BusinessService;
import com.stalion73.service.SupplierService;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.stalion73.model.Business;
import com.stalion73.model.Supplier;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.mediatype.problem.Problem;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/suppliers")
public class SupplierController {

    @Autowired
    private final SupplierService supplierService;
    private final BusinessService businessService;

    private final static HttpHeaders headers = new HttpHeaders();


    public  static void setup(){
        // headers.setAccessControlAllowOrigin("*");
   	}

    public SupplierController(SupplierService supplierService, BusinessService businessService){
        this.supplierService = supplierService;
        this.businessService = businessService;
    }

    @RequestMapping(value = "", method = RequestMethod.GET, produces = "application/json")
    public ResponseEntity<?> all() {
        SupplierController.setup();
        Collection<Supplier> suppliers = this.supplierService.findAll();
        if (suppliers.isEmpty()) {
            return ResponseEntity
                    .status(HttpStatus.NO_CONTENT)
                    .headers(headers)
                    .body(suppliers);
        }else{
            return ResponseEntity
                    .status(HttpStatus.OK)
                    .headers(headers)
                    .body(suppliers);
        }

    }

    @RequestMapping(value = "/{id}", method = RequestMethod.GET, produces = "application/json")
    public ResponseEntity<?> one(@PathVariable("id") Integer id) {
        SupplierController.setup();
        Optional<Supplier> supplier = this.supplierService.findById(id);
        Business business = this.businessService.findBusinessBySupplierId(supplier.get().getId());
        HttpHeaders headers = new HttpHeaders();
        if (!supplier.isPresent()) {
            return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .header(HttpHeaders.CONTENT_TYPE, MediaTypes.HTTP_PROBLEM_DETAILS_JSON_VALUE)
                .headers(headers)
                .body(Problem.create()
                    .withTitle("Ineffected ID")
                    .withDetail("The provided ID doesn't exist"));
        }else{
            headers.add("business_id", toJSON(business.getId().toString()));
            return ResponseEntity
                .status(HttpStatus.OK) 
                .headers(headers) 
                .body(supplier.get());
        }
    }

    @RequestMapping(value = "", method = RequestMethod.POST, produces = "application/json")
    public ResponseEntity<?> create(@Valid @RequestBody Supplier supplier,
                                            BindingResult bindingResult, 
                                            UriComponentsBuilder ucBuilder) {

        SupplierController.setup();                                        
        BindingErrorsResponse errors = new BindingErrorsResponse();
        HttpHeaders headers = new HttpHeaders();
        if (bindingResult.hasErrors() || ( supplier== null)) {
            errors.addAllErrors(bindingResult);
            headers.add("errors", errors.toJSON());
            return ResponseEntity
				.status(HttpStatus.BAD_REQUEST)
				.headers(headers)
				.body(Problem.create()
					.withTitle("Validation error")
					.withDetail("The provided consumer was not successfuly validated"));
        }else{
            this.supplierService.save(supplier);
            headers.setLocation(ucBuilder.path("/business").buildAndExpand(supplier.getId()).toUri());
            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .headers(headers)
                    .body(supplier);
        }
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.PUT, produces = "application/json")
	public ResponseEntity<?> update(@PathVariable("id") Integer id, 
                                            @RequestBody @Valid Supplier newSupplier, 
                                            BindingResult bindingResult){

        SupplierController.setup();
		BindingErrorsResponse errors = new BindingErrorsResponse();                                        
        if(bindingResult.hasErrors() || (newSupplier == null)){
            errors.addAllErrors(bindingResult);
            headers.add("errors", errors.toJSON());
            return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .headers(headers)
                .body(Problem.create()
                    .withTitle("Validation error")
                    .withDetail("The provided consumer was not successfuly validated"));
        }else if(!this.supplierService.findById(id).isPresent()){
            return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .header(HttpHeaders.CONTENT_TYPE, MediaTypes.HTTP_PROBLEM_DETAILS_JSON_VALUE)
                .headers(headers)
                .body(Problem.create()
                    .withTitle("Ineffected ID")
                    .withDetail("The provided ID doesn't exist"));
        }else{
    
            this.supplierService.update(id, newSupplier);
            Supplier updatedSupplier = this.supplierService.findById(id).get();
            return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT)
					.headers(headers)
					.body(updatedSupplier);
        }
	}

    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE, produces = "application/json")
	public ResponseEntity<?> delete(@PathVariable("id") Integer id){
		SupplierController.setup();
		Optional<Supplier> supplier = this.supplierService.findById(id);
		if(supplier.isPresent()){
            Business spBusiness = this.businessService.findBusinessBySupplierId(id);
            this.businessService.delete(spBusiness);
            this.supplierService.deleteById(id);
			return ResponseEntity.noContent().build();
		}else{
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .header(HttpHeaders.CONTENT_TYPE, MediaTypes.HTTP_PROBLEM_DETAILS_JSON_VALUE)
                    .headers(headers)
                    .body(Problem.create()
                        .withTitle("Ineffected ID")
                        .withDetail("The provided ID doesn't exist"));
        }
	}

    // -------------AUGUSTO'S BAD&IMPROVABLE CODE------------

	@RequestMapping(value = "/profile", method = RequestMethod.GET,produces = "application/json")
	public ResponseEntity<?> profile(SecurityContextHolder contextHolder){

		String username = (String) contextHolder.getContext().getAuthentication().getPrincipal();
		Supplier supplier = this.supplierService.findSupplierByUsername(username);
		if(supplier!=null){
		
		return ResponseEntity
				.status(HttpStatus.OK)
				.headers(headers) 
				.body(supplier);
		}else {
			return ResponseEntity
				.status(HttpStatus.NOT_FOUND)
				.headers(headers).body("Sorry");
		}
	}

    public String toJSON(String s) {
		ObjectMapper mapper = new ObjectMapper();
		mapper.setVisibility(PropertyAccessor.FIELD, Visibility.ANY);
		String json = "";
		try {
			json = mapper.writeValueAsString(s);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		return json;
	}
    
}
