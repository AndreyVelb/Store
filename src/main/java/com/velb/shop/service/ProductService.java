package com.velb.shop.service;

import com.velb.shop.exception.ProductChangingException;
import com.velb.shop.exception.ProductNotFoundException;
import com.velb.shop.model.converter.HashtagsFromSetConverter;
import com.velb.shop.model.converter.HashtagsToSetConverter;
import com.velb.shop.model.dto.ProductAmountUpdatingDto;
import com.velb.shop.model.dto.ProductCreatingDto;
import com.velb.shop.model.dto.ProductDeletingDto;
import com.velb.shop.model.dto.ProductForMessageDto;
import com.velb.shop.model.dto.ProductUpdatingDto;
import com.velb.shop.model.entity.BasketElement;
import com.velb.shop.model.entity.Product;
import com.velb.shop.model.entity.User;
import com.velb.shop.repository.BasketElementRepository;
import com.velb.shop.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ProductService {
    private final ProductRepository productRepository;
    private final BasketElementRepository basketElementRepository;
    private final HashtagsFromSetConverter hashtagsFromSetConverter;
    private final HashtagsToSetConverter hashtagsToSetConverter;

    @Transactional
    public Long createProduct(ProductCreatingDto productCreatingDto) {
        Product newProduct = productRepository.save(
                Product.builder()
                        .title(productCreatingDto.getTitle())
                        .description(productCreatingDto.getDescription())
                        .price(productCreatingDto.getPrice())
                        .amount(productCreatingDto.getAmount())
                        .hashtags(hashtagsFromSetConverter
                                .convert(new HashSet<>(productCreatingDto.getHashtags())))
                        .build());
        return newProduct.getId();
    }

    @Transactional
    public void updateProduct(ProductUpdatingDto updatingDto) {
        Product productFromDb = productRepository.findByIdWithPessimisticLock(updatingDto.getProductId()).orElseThrow(()
                -> new ProductNotFoundException("Товара с id " + updatingDto.getProductId() + " не существует; "));

        List<BasketElement> basketElementsHavingUpdatedProduct =
                basketElementRepository.findAllByProductIdNotOrdered(updatingDto.getProductId());

        if (basketElementsHavingUpdatedProduct.isEmpty() || updatingDto.isCanBeUpdated()) {
            Product updatedProduct = Product.builder()
                    .id(updatingDto.getProductId())
                    .title(updatingDto.getTitle() == null ? productFromDb.getTitle() : updatingDto.getTitle())
                    .description(updatingDto.getDescription() == null ? productFromDb.getDescription() : updatingDto.getDescription())
                    .price(updatingDto.getPrice() == null ? productFromDb.getPrice() : updatingDto.getPrice())
                    .amount(productFromDb.getAmount())
                    .build();

            if (!updatingDto.getHashtagsAsString().isEmpty()) {
                Set<String> allHashtags;
                if (productFromDb.getHashtags() != null) {
                    allHashtags = new HashSet<>(Objects.requireNonNull(hashtagsToSetConverter.convert(productFromDb.getHashtags())));
                    allHashtags.addAll(new HashSet<>(updatingDto.getHashtagsAsString()));
                } else {
                    allHashtags = new HashSet<>(updatingDto.getHashtagsAsString());
                }
                updatedProduct.setHashtags(hashtagsFromSetConverter.convert(allHashtags));
            }

            productRepository.save(updatedProduct);

        } else
            throw new ProductChangingException("Товар находится у кого-то в корзине а вы не указали что хотите изменить его в любом случае; ");
    }

    @Transactional
    public void updateProductAmount(ProductAmountUpdatingDto updatingDto) {
        Product productFromDb = productRepository.findByIdWithPessimisticLock(updatingDto.getProductId()).orElseThrow(()
                -> new ProductNotFoundException("Товара с id " + updatingDto.getProductId() + " не существует; "));

        if (productFromDb.getAmount() + updatingDto.getUpdateAmount() >= 0) {
            productFromDb.setAmount(productFromDb.getAmount() + updatingDto.getUpdateAmount());
            productRepository.save(productFromDb);
        } else throw new ProductChangingException("Вы пытаетесь изменить количество товара на недопустимое значение " +
                "- оно станет меньше 0; ");
    }

    // На товар и его характеристики ссылаются BasketElements уже созданных заказов,
    // поэтому при удалении количество товара становится 0
    // и удаляются все BasketElements в которые он заказан, но не добавлен
    @Transactional
    public void deleteProduct(ProductDeletingDto deletingDto) {
        Product productForDelete = productRepository.findByIdWithPessimisticLock(deletingDto.getProductId()).orElseThrow(()
                -> new ProductNotFoundException("Товара с id " + deletingDto.getProductId() + " не существует; "));

        List<BasketElement> basketElementsThatContainProductForDeleting =
                basketElementRepository.findAllByProductIdNotOrdered(deletingDto.getProductId());

        if (basketElementsThatContainProductForDeleting.isEmpty() || deletingDto.isCanBeDeleted()) {
            if (!basketElementsThatContainProductForDeleting.isEmpty()) {
                basketElementRepository.deleteAll(basketElementsThatContainProductForDeleting);
            }

            productForDelete.setAmount(0);
            productForDelete.setPrice(0);
        } else {
            throw new ProductChangingException("Товар находится у кого-то в корзине " +
                    "а вы не указали что хотите удалить его в любом случае; ");
        }
    }

    @Transactional(readOnly = true)
    public Set<User> getConsumersWhoHaveThisProductInBasket(Long productId) {
        List<BasketElement> basketElementsThatContainThisProduct = basketElementRepository.findAllFetchConsumerByProductId(productId);
        Set<User> consumersThatContainThisProductInBasket = new HashSet<>();
        basketElementsThatContainThisProduct.forEach(basketElement ->
                consumersThatContainThisProductInBasket.add(basketElement.getConsumer()));
        return consumersThatContainThisProductInBasket;
    }

    @Transactional(readOnly = true)
    public ProductForMessageDto getProductDto(Long productId) {
        return productRepository.findProductDtoById(productId).orElseThrow(()
                -> new ProductNotFoundException("Товара с id " + productId + " не существует; "));
    }

}
