package org.wikitolearn.midtier.course.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;
import org.wikitolearn.midtier.course.client.PageClient;
import org.wikitolearn.midtier.course.entity.EntityList;
import org.wikitolearn.midtier.course.entity.Page;
import org.wikitolearn.midtier.course.event.PageDeleted;
import org.wikitolearn.midtier.course.event.PageUpdated;

import com.fasterxml.jackson.core.JsonProcessingException;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class PageService {
  @Autowired
  private ApplicationEventPublisher applicationEventPublisher;

  @Autowired
  private PageClient pageClient;

  public EntityList<Page> findAll() {
    return pageClient.findAll();
  }

  public Page find(String pageId, MultiValueMap<String, String> params) {
    return pageClient.find(pageId, params);
  }

  public List<Page> save(List<Page> pages) {
    pages.stream().forEach(p -> {
      Page saved;
      try {
        saved = this.save(p);
        p.setId(saved.getId());
        p.setVersion(saved.getVersion());
      } catch (JsonProcessingException e) {
        log.error(e.getMessage());
      }
    });
    return pages;
  }

  public Page save(Page page) throws JsonProcessingException {
    return pageClient.store(page);
  }

  public Page update(Page page) throws JsonProcessingException {
    Page updatedPage = pageClient.update(page);
    applicationEventPublisher.publishEvent(new PageUpdated(this, updatedPage));
    return updatedPage;
  }
  
  public Page delete(Page page, boolean isBulkDelete) throws JsonProcessingException {
    Page deletedPage = pageClient.delete(page);
    if(!isBulkDelete) {
      applicationEventPublisher.publishEvent(new PageDeleted(this, page));
    }
    return deletedPage;
  }
}
