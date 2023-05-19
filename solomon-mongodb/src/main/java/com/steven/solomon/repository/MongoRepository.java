package com.steven.solomon.repository;

import com.steven.solomon.pojo.enums.OrderByEnum;
import com.steven.solomon.template.DynamicMongoTemplate;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.steven.solomon.pojo.param.BasePageParam;
import com.steven.solomon.pojo.vo.PageVO;
import com.steven.solomon.verification.ValidateUtils;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

public class MongoRepository<T, I> {

  protected Class<T> modelClass = (Class) ((ParameterizedType) this.getClass().getGenericSuperclass()).getActualTypeArguments()[0];

  protected static String ID = "_id";

  private final MongoTemplate mongoTemplate;

  public MongoRepository(MongoTemplate mongoTemplate) {this.mongoTemplate = mongoTemplate;}

  /**
   * 获取一条记录
   */
  public T get(Query query) {
    return mongoTemplate.findOne(query, modelClass);
  }

  /**
   * 获取一条记录
   */
  public <V> V get(Query query, Class<V> clazz) {
    return mongoTemplate.findOne(query, clazz);
  }

  /**
   * 根据id获取记录
   */
  public T getById(I id) {
    Query query = new Query();
    query.addCriteria(Criteria.where(ID).is(id));
    return mongoTemplate.findOne(query, modelClass);
  }

  /**
   * 根据id获取记录
   */
  public <V> V getById(I id, Class<V> clazz) {
    Query query = new Query();
    query.addCriteria(Criteria.where(ID).is(id));
    return mongoTemplate.findOne(query, clazz);
  }

  /**
   * 查询列表
   */
  public List<T> find(Query query) {
    return mongoTemplate.find(query, modelClass);
  }

  /**
   * 查询列表
   */
  public <V> List<V> find(Query query, Class<V> clazz) {
    return mongoTemplate.find(query, clazz);
  }

  /**
   * 保存(非覆盖保存)
   */
  public T save(T entity) {
    return mongoTemplate.save(entity);
  }

  /**
   * 保存(非覆盖保存)
   */
  public Collection<T> save(Collection<T> entity) {
    entity.forEach(data -> {
      this.save(data);
    });
    return entity;
  }

  /**
   * 保存(覆盖保存)
   */
  public T insert(T entity) {
    return mongoTemplate.insert(entity);
  }

  /**
   * 保存(覆盖保存)
   */
  public Collection<T> insert(Collection<T> entity) {
    return mongoTemplate.insertAll(entity);
  }

  /**
   * 统计记录数
   *
   * @param query
   * @return
   */
  public long count(Query query) {
    return mongoTemplate.count(query, modelClass);
  }

  /**
   * 根据id删除记录
   */
  public void deleteById(I id) {
    Query query = new Query(Criteria.where("_id").is(id));
    mongoTemplate.remove(query, modelClass);
  }

  /**
   * 删除记录
   */
  public void delete(Query query) {
    mongoTemplate.remove(query, modelClass);
  }

  /**
   * 更新记录
   *
   * @param query
   * @param update
   */
  public void update(Query query, Update update) {
    mongoTemplate.updateFirst(query, update, modelClass);
  }

  /**
   * 判断记录是否存在
   */
  public boolean exists(Query query) {
    long count = count(query);
    return count > 0 ? true : false;
  }

  /**
   * 分组查询
   */
  public List<T> aggregate(List<AggregationOperation> list) {
    return aggregate(list,modelClass);
  }

  /**
   * 分组查询
   */
  public List<T> aggregate(List<AggregationOperation> list,Class<T> clazz) {
    AggregationResults<T> results = mongoTemplate.aggregate(Aggregation.newAggregation(list), mongoTemplate.getCollectionName(modelClass), clazz);
    return results.getMappedResults();
  }

  /**
   * 普通分页查询
   */
  public PageVO<T> page(Query query, BasePageParam basePageParam) {
    return page(query,basePageParam,modelClass);
  }

  /**
   * 普通分页查询
   */
  public PageVO<T> page(Query query, BasePageParam basePageParam,Class<T> clazz) {
    //统计记录数
    long   total = count(query);
    PageVO page  = new PageVO<>(null, total, basePageParam.getPageNo(), basePageParam.getPageSize());
    if (total <= 0) {
      return page;
    }

    query.skip((basePageParam.getPageNo() - 1) * basePageParam.getPageSize()).limit(basePageParam.getPageSize());

    List<BasePageParam.Sort> sorted = basePageParam.getSorted();
    if (ValidateUtils.isNotEmpty(sorted)) {
      query.with(Sort.by(getSort(basePageParam)));
    }
    page.setData(this.find(query,clazz));
    return page;
  }

  public PageVO<T> aggregate(List<AggregationOperation> list, Criteria criteria,BasePageParam basePageParam) {
    return aggregate(list,criteria,basePageParam,modelClass);
  }

  public PageVO<T> aggregate(List<AggregationOperation> list, Criteria criteria,BasePageParam basePageParam,Class<T> clazz) {
    long   total = count(ValidateUtils.isNotEmpty(criteria) ? new Query(criteria) : new Query());
    PageVO page  = new PageVO<>(null, total, basePageParam.getPageNo(), basePageParam.getPageSize());
    if (total <= 0) {
      return page;
    }
    list.addAll(page(basePageParam));
    AggregationResults<T> results = mongoTemplate.aggregate(Aggregation.newAggregation(list), mongoTemplate.getCollectionName(modelClass), clazz);
    page.setData(results.getMappedResults());
    return page;
  }

  public Sort sort(BasePageParam basePageParam) {
    List<BasePageParam.Sort> sorted        = basePageParam.getSorted();
    if (ValidateUtils.isNotEmpty(sorted)) {
      return Sort.by(getSort(basePageParam));
    } else {
      List<Sort.Order> orders = null;
      return Sort.by(orders);
    }
  }

  //获取多个排序方式
  public List<Sort.Order> getSort(BasePageParam basePageParam) {
    List<BasePageParam.Sort> sorted        = basePageParam.getSorted();
    List<Sort.Order>         querySortList = new ArrayList<>();
    if (ValidateUtils.isNotEmpty(sorted)) {
      for (BasePageParam.Sort sort : sorted) {
        String      orderByField = sort.getOrderByField();
        OrderByEnum orderBy      = sort.getOrderByMethod();
        if (orderBy.name().equalsIgnoreCase(OrderByEnum.DESCEND.name())) {
          querySortList.add(Sort.Order.desc(orderByField));
        } else {
          querySortList.add(Sort.Order.asc(orderByField));
        }
      }
    }
    return querySortList;
  }

  public List<AggregationOperation> page(BasePageParam param){
    List<AggregationOperation> aggregationList = new ArrayList<>();
    int skip = (param.getPageNo() - 1) * param.getPageSize();

    aggregationList.add(Aggregation.skip(Long.valueOf(skip)));
    aggregationList.add(Aggregation.limit(param.getPageSize()));
    return aggregationList;
  }
}
