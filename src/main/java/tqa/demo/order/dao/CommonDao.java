package tqa.demo.order.dao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class CommonDao {

	@Autowired
//	protected SqlSession sqlSession;
	protected SqlSessionTemplate sqlSession;
	
	public int insert(String queryId, Object parameterObject) {
		return sqlSession.insert(queryId, parameterObject);
	}
	
	public int update(String queryId, Object paramObject) {
		return sqlSession.update(queryId, paramObject);
	}
	
	public int delete(String queryId, Object paramObject) {
		return sqlSession.delete(queryId, paramObject);
	}
	
	public <T> T selectOne(String queryId, Object paramObject) {
		return sqlSession.selectOne(queryId, paramObject);
	}
	
	public <E> List<E> selectList(String queryId, Object paramObject){
		return sqlSession.selectList(queryId, paramObject);
	}
	
	public <E> List<E> selectList(String queryId, String fromDate, String toDate){
		
		Map<String, String> paramObject = new HashMap<>();
		paramObject.put("fromDate", fromDate);
		paramObject.put("toDate", toDate);
		return sqlSession.selectList(queryId, paramObject);
	}
}
