package com.product.service;

import com.product.client.InventoryClient;
import com.product.dtos.InventoryInputDTO;
import com.product.dtos.ProductRequestDTO;
import com.product.dtos.ProductResponseDTO;
import com.product.dtos.ProductResponseReturn;
import com.product.entities.Product;
import com.product.exception.CategoryNotFoundException;
import com.product.exception.InventoryServiceException;
import com.product.exception.ProductNotFoundException;
import com.product.exception.UserIdNotFoundException;
import com.product.repository.ProductRepository;
import feign.FeignException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService{

//    private final ModelMapper modelMapper;
    private final ProductRepository productRepository;
    private final InventoryClient inventoryClient;


    @Override
    public List<ProductResponseReturn> getAll() {
        List<Product> product=productRepository.findAll();

        List<ProductResponseReturn> response=product.stream()
                .map(ele->{
                    ProductResponseReturn dto=new ProductResponseReturn();
                    dto.setId(ele.getId());
                    dto.setCategory(ele.getCategory());
                    dto.setName(ele.getName());
                    dto.setPrice(ele.getPrice());
                    dto.setImageurl(ele.getImageurl());
                    dto.setDescription(ele.getDescription());
                    return dto;
                }).toList();
        return response;

        //return product.stream().map(ele->modelMapper.map(ele,ProductResponseDTO.class)).toList();
    }

    @Override
    public ProductResponseDTO getByProductId(Long id) {
        Product product=productRepository.findById(id).orElseThrow(()->new UserIdNotFoundException("Product Id Not Found"));
        Integer quantity=inventoryClient.getQuantity(id).getBody();
        ProductResponseDTO response=new ProductResponseDTO();
        response.setId(product.getId());
        response.setDescription(product.getDescription());
        response.setName(product.getName());
        response.setCategory(product.getCategory());
        response.setPrice(product.getPrice());
        response.setImageurl(product.getImageurl());
        response.setStock(quantity);
        return response;

        //return modelMapper.map(product,ProductResponseDTO.class);
    }


    @Override
    @Transactional(rollbackFor = InventoryServiceException.class)
    @CircuitBreaker(name = "gateway", fallbackMethod = "getByIdFallBack")
    public List<ProductResponseDTO> getById(Long id) {
        List<Product> product=productRepository.findBySellerId(id).orElseThrow(()->new UserIdNotFoundException("Seller Id not found"));

        List<ProductResponseDTO> response=product.stream()
                .map(ele->{
                    ProductResponseDTO dto=new ProductResponseDTO();
                    dto.setId(ele.getId());
                    dto.setName(ele.getName());
                    dto.setDescription(ele.getDescription());
                    dto.setPrice(ele.getPrice());
                    dto.setCategory(ele.getCategory());
                    dto.setImageurl(ele.getImageurl());
                    try {
                        dto.setStock(inventoryClient.getQuantity(ele.getId()).getBody());
                    }
                    catch (FeignException ex){
                        dto.setStock(null);
                    }
                    return dto;

                }).toList();

        return response;


        //return product.stream().map(ele->modelMapper.map(ele,ProductResponseDTO.class)).toList();
    }

    @Override
    public List<ProductResponseReturn> getByCategories(String categories) {
       List<Product> product =productRepository.findByCategory(categories)
               .orElseThrow(()->new CategoryNotFoundException("Such Category is not present"));
        List<ProductResponseReturn> response=product.stream()
                .map(ele->{
                    ProductResponseReturn dto=new ProductResponseReturn();
                    dto.setId(ele.getId());
                    dto.setCategory(ele.getCategory());
                    dto.setName(ele.getName());
                    dto.setPrice(ele.getPrice());
                    dto.setImageurl(ele.getImageurl());
                    dto.setDescription(ele.getDescription());
                    return dto;
                }).toList();
        return response;




       //return product.stream().map(ele->modelMapper.map(ele, ProductResponseDTO.class)).collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = InventoryServiceException.class)
    @CircuitBreaker(name = "gateway", fallbackMethod = "addProductFallback")
    public ProductResponseDTO addProduct(Long sellerId,ProductRequestDTO dto) {

        Product product=new Product();
        product.setName(dto.getName());
        product.setImageurl(dto.getImageurl());
        product.setCategory(dto.getCategory());
        product.setPrice(dto.getPrice());
        product.setDescription(dto.getDescription());
        product.setSellerId(sellerId);
        Product product1=productRepository.save(product);
        InventoryInputDTO inputDTO=new InventoryInputDTO();
        inputDTO.setProductid(product1.getId());
        inputDTO.setQuantity(dto.getStock());
        inventoryClient.change(inputDTO);
        ProductResponseDTO response=new ProductResponseDTO();
        response.setId(product1.getId());
        response.setName(product1.getName());
        response.setPrice(product1.getPrice());
        response.setDescription(product1.getDescription());
        response.setCategory(product1.getCategory());
        response.setStock(dto.getStock());
        response.setImageurl(product1.getImageurl());
        return response;





//        Product product=modelMapper.map(dto,Product.class);
//        product.setSellerId(sellerId);
//        Product product1 =productRepository.save(product);
//        InventoryInputDTO inputDTO=new InventoryInputDTO();
//        inputDTO.setProductid(product1.getId());
//        inputDTO.setQuantity(dto.getStock());
//        inventoryClient.change(inputDTO);
//        return modelMapper.map(product1,ProductResponseDTO.class);
    }



    @Override
    @Transactional(rollbackFor = InventoryServiceException.class)
    @CircuitBreaker(name = "gateway",fallbackMethod = "updateProductFallBack")
    public ProductResponseDTO updateProduct(Long sellerId, ProductRequestDTO dto,Long id) {
        List<Product> product=productRepository.findBySellerId(sellerId).orElseThrow(()->new UserIdNotFoundException("SellerId not found"));

        Product filterProduct=product.stream().filter(ele->ele.getId().equals(id)).findFirst().orElseThrow(()->new ProductNotFoundException("ProductId is not Present"));

        ProductResponseDTO responseDTO=new ProductResponseDTO();
        responseDTO.setName(filterProduct.getName());
        responseDTO.setId(filterProduct.getId());
        responseDTO.setPrice(filterProduct.getPrice());
        responseDTO.setCategory(filterProduct.getCategory());
        responseDTO.setDescription(filterProduct.getDescription());
        responseDTO.setImageurl(filterProduct.getImageurl());
        InventoryInputDTO inputDTO=new InventoryInputDTO();
        inputDTO.setProductid(filterProduct.getId());
        inputDTO.setQuantity(dto.getStock());
        inventoryClient.change(inputDTO);
        responseDTO.setStock(inventoryClient.getQuantity(filterProduct.getId()).getBody());
        return responseDTO;


        //       Product product1= product.stream().filter(ele->ele.getId().equals(id))
//               .map(ele->{
//            ele.setName(dto.getName());
//            ele.setCategory(dto.getCategory());
//            ele.setStock(dto.getStock());
//            ele.setDescription(dto.getDescription());
//            ele.setPrice(dto.getPrice());
//            ele.setImageurl(dto.getImageurl());
//            return productRepository.save(ele);
//        }).findFirst()
//               .orElseThrow(()->new UserIdNotFoundException("Product Id Not Found"));
//        return modelMapper.map(product,ProductResponseDTO.class);
    }

    @Override
    public void deleteById(Long sellerId,Long id) {
        List<Product> product=productRepository.findBySellerId(sellerId).orElseThrow(()->new UserIdNotFoundException("SellerId not found"));
        Product product1= product.stream().filter(ele->ele.getId().equals(id))
                .findFirst().orElseThrow(()->new ProductNotFoundException("ProductId not found"));
        productRepository.deleteById(id);

    }

    public ProductResponseDTO addProductFallback(Long sellerId, ProductRequestDTO dto, Throwable th) {
        if(th instanceof ProductNotFoundException || th instanceof UserIdNotFoundException)
            throw (RuntimeException) th;
        System.err.println("Fallback triggered. Rolling back transaction...");
        throw new InventoryServiceException("Inventory Service is currently down. Product creation cancelled for safety.");
    }

    public ProductResponseDTO updateProductFallBack(Long sellerId, ProductRequestDTO dto,Long id,Throwable th){
        if(th instanceof ProductNotFoundException || th instanceof UserIdNotFoundException)
            throw (RuntimeException) th;
        System.err.println("Fallback triggered. Rolling back transaction...");

        throw new InventoryServiceException("Inventory Service is currently down. Product update cancelled for safety.");
    }

    public List<ProductResponseDTO> getByIdFallBack(Long id,Throwable th){
        if(th instanceof ProductNotFoundException || th instanceof UserIdNotFoundException)
            throw (RuntimeException) th;
        throw new InventoryServiceException("Inventory Service is currently down.");
    }





}
