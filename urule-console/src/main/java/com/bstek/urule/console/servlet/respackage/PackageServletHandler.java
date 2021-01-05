/*******************************************************************************
 * Copyright 2017 Bstek
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.  You may obtain a copy
 * of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
package com.bstek.urule.console.servlet.respackage;

import java.awt.Color;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.*;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.bstek.urule.BizUtils;
import com.bstek.urule.knowledge.KnowledgeHelper;
import com.bstek.urule.model.library.action.ActionConfig;
import com.bstek.urule.model.library.variable.*;
import com.bstek.urule.model.rete.JsonUtils;
import com.bstek.urule.model.rule.*;
import com.bstek.urule.model.rule.lhs.*;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.lang.StringUtils;
import org.apache.jackrabbit.commons.json.JsonUtil;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.codehaus.jackson.map.ObjectMapper;

import com.bstek.urule.RuleException;
import com.bstek.urule.Utils;
import com.bstek.urule.builder.KnowledgeBase;
import com.bstek.urule.builder.KnowledgeBuilder;
import com.bstek.urule.builder.ResourceBase;
import com.bstek.urule.console.EnvironmentUtils;
import com.bstek.urule.console.User;
import com.bstek.urule.console.repository.RepositoryService;
import com.bstek.urule.console.repository.RepositoryServiceImpl;
import com.bstek.urule.console.repository.model.ResourcePackage;
import com.bstek.urule.console.servlet.RenderPageServletHandler;
import com.bstek.urule.console.servlet.RequestContext;
import com.bstek.urule.model.GeneralEntity;
import com.bstek.urule.model.flow.FlowDefinition;
import com.bstek.urule.model.library.Datatype;
import com.bstek.urule.runtime.KnowledgePackage;
import com.bstek.urule.runtime.KnowledgeSession;
import com.bstek.urule.runtime.KnowledgeSessionFactory;
import com.bstek.urule.runtime.cache.CacheUtils;
import com.bstek.urule.runtime.response.ExecutionResponse;
import com.bstek.urule.runtime.response.ExecutionResponseImpl;
import org.springframework.util.CollectionUtils;

/**
 * @author Jacky.gao
 * @since 2016年6月3日
 */
public class PackageServletHandler extends RenderPageServletHandler {
	public static final String KB_KEY="_kb";
	public static final String VCS_KEY="_vcs";
	public static final String IMPORT_EXCEL_DATA="_import_excel_data";
	private RepositoryService repositoryService;
	private KnowledgeBuilder knowledgeBuilder;
	private HttpSessionKnowledgeCache httpSessionKnowledgeCache;

	private KnowledgeHelper knowledgeHelper;
	@Override
	public void execute(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String method=retriveMethod(req);
		if(method!=null){
			invokeMethod(method, req, resp);
		}else{
			VelocityContext context = new VelocityContext();
			context.put("contextPath", req.getContextPath());
			resp.setContentType("text/html");
			resp.setCharacterEncoding("utf-8");
			Template template=ve.getTemplate("html/package-editor.html","utf-8");
			PrintWriter writer=resp.getWriter();
			template.merge(context, writer);
			writer.close();
		}
	}
	public void loadPackages(HttpServletRequest req, HttpServletResponse resp) throws Exception {
		String project=req.getParameter("project");
		project=Utils.decodeURL(project);
		List<ResourcePackage> packages=repositoryService.loadProjectResourcePackages(project);
		writeObjectToJson(resp, packages);
	}
	
	@SuppressWarnings("unchecked")
	public void exportExcelTemplate(HttpServletRequest req, HttpServletResponse resp) throws Exception {
		List<VariableCategory> variableCategories=(List<VariableCategory>)httpSessionKnowledgeCache.get(req, VCS_KEY);
		if(variableCategories==null){
			KnowledgeBase knowledgeBase=buildKnowledgeBase(req);
			variableCategories=knowledgeBase.getResourceLibrary().getVariableCategories();
		}
		SXSSFWorkbook wb = new SXSSFWorkbook();
		XSSFCellStyle style=(XSSFCellStyle)wb.createCellStyle();
		Color c=new Color(147,208,15);
		XSSFColor xssfColor=new XSSFColor(c);
		style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
		style.setFillForegroundColor(xssfColor);
		for(VariableCategory vc:variableCategories){
			buildSheet(wb, vc,style);
		}
		resp.setContentType("application/x-xls");
		resp.setHeader("Content-Disposition","attachment; filename=urule-batch-test-template.xlsx");
		OutputStream outputStream=resp.getOutputStream();
		wb.write(outputStream);;
		outputStream.flush();
		outputStream.close();
	}
	
	private void buildSheet(SXSSFWorkbook wb,VariableCategory vc,XSSFCellStyle style){
		String name=vc.getName();
		Sheet sheet=wb.createSheet(name);
		Row row=sheet.createRow(0);
		List<Variable> variables=vc.getVariables();
		for(int i=0;i<variables.size();i++){
			sheet.setColumnWidth(i,4000);
			Cell cell=row.createCell(i);
			Variable var=variables.get(i);
			cell.setCellValue(var.getLabel());
			cell.setCellStyle(style);
		}
	}
	
	public void importExcelTemplate(HttpServletRequest req, HttpServletResponse resp) throws Exception {
		DiskFileItemFactory factory=new DiskFileItemFactory();
		ServletFileUpload upload = new ServletFileUpload(factory);
		List<FileItem> items = upload.parseRequest(req);
		Iterator<FileItem> itr = items.iterator();
		List<Map<String,Object>> mapData=null;
		while (itr.hasNext()) {
			FileItem item = (FileItem) itr.next();
			String name=item.getFieldName();
			if(!name.equals("file")){
				continue;
			}
			InputStream stream=item.getInputStream();
			mapData=parseExcel(stream);
			httpSessionKnowledgeCache.put(req, IMPORT_EXCEL_DATA, mapData);
			stream.close();
			break;
		}
		httpSessionKnowledgeCache.put(req, IMPORT_EXCEL_DATA, mapData);
		writeObjectToJson(resp, mapData);
	}

	@SuppressWarnings("resource")
	private List<Map<String,Object>> parseExcel(InputStream stream) throws Exception {
		List<Map<String,Object>> mapList=new ArrayList<Map<String,Object>>();
		XSSFWorkbook wb = new XSSFWorkbook(stream);
		for (int i = 0; i < wb.getNumberOfSheets(); i++) {
			XSSFSheet sheet = wb.getSheetAt(i);
			if (sheet == null) {
				continue;
			}
			String name = sheet.getSheetName();
			Map<String,Object> map=new HashMap<String,Object>();
			map.put("name",name);
			map.put("data", buildVariables(sheet));
			mapList.add(map);
		}
		return mapList;
	}
	
	private List<Map<String,String>> buildVariables(XSSFSheet sheet){
		Map<Integer,String> headerMap=new HashMap<Integer,String>();
		List<Map<String,String>> mapList=new ArrayList<Map<String,String>>();
		int totalRow=sheet.getLastRowNum();
		XSSFRow headerRow=sheet.getRow(0);
		int totalColumn=headerRow.getLastCellNum();
		Map<String,String> noDataRowMap=new HashMap<String,String>();
		for(int i=0;i<totalColumn;i++){
			XSSFCell cell=headerRow.getCell(i);
			String value=cell.getStringCellValue();
			headerMap.put(i, value);
			String headerName=value.replaceAll("\\.", "-");
			noDataRowMap.put(headerName, null);
		}
		for(int i=1;i<=totalRow;i++){
			XSSFRow row=sheet.getRow(i);
			if(row==null){
				continue;
			}
			Map<String,String> map=new HashMap<String,String>(); 
			mapList.add(map);
			for(int j=0;j<totalColumn;j++){
				XSSFCell cell=row.getCell(j);
				String headerName=headerMap.get(j);
				if(headerName==null){
					continue;
				}
				if(cell==null){
					headerName=headerName.replaceAll("\\.", "-");
					map.put(headerName, "");
				}else{
					String value="";
					int cellType=cell.getCellType();
					switch(cellType){
					case Cell.CELL_TYPE_STRING:
						value=cell.getStringCellValue();
						break;
					case Cell.CELL_TYPE_BLANK:
						value="";
						break;
					case Cell.CELL_TYPE_BOOLEAN:
						value=String.valueOf(cell.getBooleanCellValue());
						break;
					case Cell.CELL_TYPE_NUMERIC:
						value=String.valueOf(cell.getNumericCellValue());
						break;
					case Cell.CELL_TYPE_ERROR:
						value="";
						break;
					case Cell.CELL_TYPE_FORMULA:
						value=cell.getCellFormula();
						break;
					}
					if(value==null){
						value="";
					}
					headerName=headerName.replaceAll("\\.", "-");
					map.put(headerName, value);
				}
			}
		}
		if(mapList.size()==0){
			mapList.add(noDataRowMap);
		}
		return mapList;
	}

	
	public void loadFlows(HttpServletRequest req, HttpServletResponse resp) throws Exception {
		KnowledgeBase knowledgeBase=(KnowledgeBase)httpSessionKnowledgeCache.get(req, KB_KEY);
		Collection<FlowDefinition> col=knowledgeBase.getFlowMap().values();
		writeObjectToJson(resp, col);
	}	
	
	public void refreshKnowledgeCache(HttpServletRequest req, HttpServletResponse resp) throws Exception {
		String project=req.getParameter("project");
		project=Utils.decodeURL(project);
		String packageId=project+"/"+Utils.decodeURL(req.getParameter("packageId"));
		if(packageId.startsWith("/")){
			packageId=packageId.substring(1,packageId.length());
		}
		KnowledgeBase knowledgeBase= buildKnowledgeBase(req);
		KnowledgePackage knowledgePackage=knowledgeBase.getKnowledgePackage();
		CacheUtils.getKnowledgeCache().putKnowledge(packageId, knowledgePackage);
		Map<String,Object> map=new HashMap<String,Object>();
		writeObjectToJson(resp, map);
	}
	
	public void loadForTestVariableCategories(HttpServletRequest req, HttpServletResponse resp) throws Exception {
		KnowledgeBase knowledgeBase = buildKnowledgeBase(req);
		List<VariableCategory> vcs=knowledgeBase.getResourceLibrary().getVariableCategories();
		httpSessionKnowledgeCache.put(req, VCS_KEY, vcs);
		writeObjectToJson(resp, vcs);
	}

	/**
	 * 构造知识库
	 * @param req
	 * @return
	 * @throws IOException
	 */
	private KnowledgeBase buildKnowledgeBase(HttpServletRequest req) throws IOException{
		String files=req.getParameter("files");
		files=Utils.decodeURL(files);
		/**
		 * 1. 解析req中规则文件名称及版本
		 */
		ResourceBase resourceBase=knowledgeBuilder.newResourceBase();
		String[] paths=files.split(";");
		for(String path:paths){
			String[] subpaths=path.split(",");
			path=subpaths[0];
			String version=null;
			if(subpaths.length>1){
				version=subpaths[1];
			}
			resourceBase.addResource(path,version);
		}
		/**
		 * 2. 解析规则
		 */
		KnowledgeBase knowledgeBase=knowledgeBuilder.buildKnowledgeBase(resourceBase);
		/**
		 * 3. 缓存
		 */
		httpSessionKnowledgeCache.remove(req, KB_KEY);
		httpSessionKnowledgeCache.put(req, KB_KEY, knowledgeBase);
		return knowledgeBase;
	}

	/**
	 * @Description 根据规则json文件生成知识包,参考常量：TEST_RULR_XML
	 *
	 * @Author wpx
	 * @Date 2020/12/22 14:14
	 * @param xml
	 * @return com.bstek.urule.builder.KnowledgeBase
	 */
	private KnowledgeBase buildKnowledgeBaseByRuleXml(HttpServletRequest req, String xml) throws IOException{

		KnowledgeBase knowledgeBase=knowledgeBuilder.buildKnowledgeBase(xml);
		/**
		 * 3. 缓存
		 */
		httpSessionKnowledgeCache.remove(req, KB_KEY);
		httpSessionKnowledgeCache.put(req, KB_KEY, knowledgeBase);
		return knowledgeBase;
	}



	/**
	 * 构建规则
	 * @return
	 */
	private Rule buildRule() {
		Rule rule = new Rule();
		rule.setName("test001");
		rule.setRemark("test001");
		rule.setLhs(this.buildLhs());
		Other other = new Other();
		other.addAction(BizUtils.buildVariableAssignAction("flag", Datatype.Boolean, "false"));
		rule.setOther(other);
		Rhs rhs = new Rhs();
		rhs.addAction(BizUtils.buildVariableAssignAction("flag", Datatype.Boolean, "true"));
		rule.setRhs(rhs);
		return rule;
	}


	/**
	 * 构建左侧规则
	 * @return
	 */
	private Lhs buildLhs(){
		Lhs lhs = new Lhs();
		And and = new And();
		Parameter parameter = BizUtils.buildSimpleParameter("username", Datatype.String ,"123" );
		and.addCriterion(BizUtils.buildMethodLeftCriteria("methodTest","evalTest",true, parameter));
		lhs.setCriterion(and);
		return lhs;
	}

	/**
	 * 保存知识包
	 *
	 * @param req
	 * @param resp
	 * @throws Exception
	 */
	public void saveResourcePackages(HttpServletRequest req, HttpServletResponse resp) throws Exception {
		String project=req.getParameter("project");
		project=Utils.decodeURL(project);
		String path=project+"/"+RepositoryServiceImpl.RES_PACKGE_FILE;
		String xml=req.getParameter("xml");
		xml=Utils.decodeURL(xml);
		User user=EnvironmentUtils.getLoginUser(new RequestContext(req,resp));
		repositoryService.saveFile(path, xml, false,null,user);
	}
	
	@SuppressWarnings("unchecked")
	private List<VariableCategory> mapToVariableCategories(List<Map<String,Object>> mapList){
		List<VariableCategory> list=new ArrayList<VariableCategory>();
		for(Map<String,Object> map:mapList){
			VariableCategory category=new VariableCategory();
			list.add(category);
			for(String key:map.keySet()){
				if(key.equals("name")){
					category.setName((String)map.get(key));
				}else if(key.equals("clazz")){
					category.setClazz((String)map.get(key));
				}else if(key.equals("variables")){
					List<Map<String,Object>> variables=(List<Map<String,Object>>)map.get(key);
					if(variables!=null){
						for(Map<String,Object> m:variables){
							Variable var=new Variable();
							category.addVariable(var);
							for(String varName:m.keySet()){
								if(varName.equals("name")){
									var.setName((String)m.get(varName));
								}else if(varName.equals("label")){
									var.setLabel((String)m.get(varName));
								}else if(varName.equals("type")){
									var.setType(Datatype.valueOf((String)m.get(varName)));
								}else if(varName.equals("defaultValue")){
									var.setDefaultValue((String)m.get(varName));
								}
							}
						}
					}
				}
			}
		}
		return list;
	}
	
	
	@SuppressWarnings("unchecked")
	public void doBatchTest(HttpServletRequest req, HttpServletResponse resp) throws Exception {
		String flowId=req.getParameter("flowId");
		List<VariableCategory> vcs=(List<VariableCategory>)httpSessionKnowledgeCache.get(req, VCS_KEY);
		if(vcs==null){
			vcs=buildKnowledgeBase(req).getResourceLibrary().getVariableCategories();
		}
		Map<String,VariableCategory> vcmap=new HashMap<String,VariableCategory>();
		for(VariableCategory vc:vcs){
			vcmap.put(vc.getName(), vc);
		}
		List<Map<String,Object>> data=(List<Map<String,Object>>)httpSessionKnowledgeCache.get(req, IMPORT_EXCEL_DATA);
		if(data==null){
			throw new RuleException("Import excel data for test has expired,please import the excel and try again.");
		}
		Map<String,List<Object>> factMap=new HashMap<String,List<Object>>();
		for(Map<String,Object> map:data){
			String name=(String)map.get("name");
			VariableCategory vc=vcmap.get(name);
			if(vc==null){
				continue;
			}
			String clazz=vc.getClazz();
			List<Map<String,Object>> rowList=(List<Map<String,Object>>)map.get("data");
			List<Variable> variables=vc.getVariables();
			List<Object> factList=new ArrayList<Object>();
			for(Map<String,Object> rowMap:rowList){
				Object entity=null;
				if(vc.getName().equals(VariableCategory.PARAM_CATEGORY)){
					entity=new HashMap<String,Object>();
				}else{
					entity=new GeneralEntity(clazz);
				}
				buildObject(entity, rowMap,variables);
				factList.add(entity);
			}
			factMap.put(name, factList);
		}
		if(factMap.size()==0){
			throw new RuleException("Import data cannot match current knowledge package.");
		}
		int rowSize=0;
		List<String> keyList=new ArrayList<String>();
		for(String key:factMap.keySet()){
			keyList.add(key);
			List<Object> facts=factMap.get(key);
			if(facts.size()>rowSize){
				rowSize=facts.size();
			}
		}
		List<Map<String,Object>> resultList=new ArrayList<Map<String,Object>>();
		int mapSize=factMap.size();
		KnowledgeBase knowledgeBase=(KnowledgeBase)httpSessionKnowledgeCache.get(req, KB_KEY);
		KnowledgePackage knowledgePackage=knowledgeBase.getKnowledgePackage();
		KnowledgeSession session=KnowledgeSessionFactory.newKnowledgeSession(knowledgePackage);
		long start=System.currentTimeMillis();
		for(int i=0;i<rowSize;i++){
			Map<String,Object> parameterMap=null;
			for(int j=0;j<mapSize;j++){
				String categoryName=keyList.get(j);
				Object fact=fetchFact(factMap,keyList,j,i);
				if(fact==null){
					continue;
				}
				if((fact instanceof Map) && !(fact instanceof GeneralEntity)){
					parameterMap=(Map<String,Object>)fact;
				}else{
					session.insert(fact);
					buildResult(resultList,categoryName,fact);
				}
			}
			if(StringUtils.isNotEmpty(flowId)){
				if(parameterMap!=null){
					session.startProcess(flowId,parameterMap);
				}else{
					session.startProcess(flowId);
				}
			}else{
				if(parameterMap==null){
					session.fireRules();			
				}else{
					session.fireRules(parameterMap);
					Map<String,Object> p=new HashMap<String,Object>();
					p.putAll(session.getParameters());
					p.remove("return_to_");
					buildResult(resultList,VariableCategory.PARAM_CATEGORY,p);
				}
			}
		}
		long end=System.currentTimeMillis();
		long elapse=end-start;
		StringBuffer sb=new StringBuffer();
		if(StringUtils.isNotEmpty(flowId)){
			sb.append("共执行规则流");
			sb.append("["+flowId+"]");
			sb.append(rowSize);
			sb.append("次,");
		}else{
			sb.append("共测试规则");
			sb.append(rowSize);
			sb.append("次,");
		}
		sb.append(""+"耗时："+elapse+"ms");
		Map<String,Object> result=new HashMap<String,Object>();
		result.put("info", sb.toString());
		result.put("data", resultList);
		writeObjectToJson(resp, result);
	}
	
	
	@SuppressWarnings("unchecked")
	private void buildResult(List<Map<String,Object>> list,String categoryName,Object fact){
		List<Object> rowList=null;
		for(Map<String,Object> map:list){
			if(map.get("name").equals(categoryName)){
				rowList=(List<Object>)map.get("data");
				break;
			}
		}
		if(rowList==null){
			rowList=new ArrayList<Object>();
			Map<String,Object> dataMap=new HashMap<String,Object>();
			dataMap.put("name", categoryName);
			dataMap.put("data", rowList);
			dataMap.put("id", UUID.randomUUID().toString());
			list.add(dataMap);
		}
		rowList.add(fact);
	}

	private Object fetchFact(Map<String,List<Object>> factMap,List<String> keyList,int i,int objectIndex){
		if(i>keyList.size()){
			return null;
		}
		String name=keyList.get(i);
		List<Object> factList = factMap.get(name);
		if(factList==null){
			return null;
		}
		if(objectIndex>=factList.size()){
			return null;
		}
		return factList.get(objectIndex);
	}
	

	private void buildObject(Object obj,Map<String,Object> map,List<Variable> variables){
		for(String name:map.keySet()){
			name=name.replaceAll("-", "\\.");
			if(name.indexOf(".")!=-1){
				instanceChildObject(obj,name);
			}
			Object value=map.get(name);
			Variable var=null;
			for(Variable variable:variables){
				if(name.equals(variable.getLabel()) || name.equals(variable.getName())){
					var=variable;
					break;
				}
			}
			if(var==null){
				throw new RuleException("Variable ["+name+"] not exist.");
			}
			Datatype type=var.getType();
			if(type.equals(Datatype.List) || type.equals(Datatype.Set) || type.equals(Datatype.Map)){
				continue;
			}
			value=type.convert(value);
			Utils.setObjectProperty(obj, var.getName(), value);
		}
	}

	/**
	 * 新测试地址
	 *
	 * @param req
	 * @param resp
	 * @throws Exception
	 */
	@SuppressWarnings({ "unchecked"})
	public void doTest(HttpServletRequest req, HttpServletResponse resp) throws Exception {
		Parameter leftPartParameter = BizUtils.buildSimpleParameter("username", Datatype.String, "张三");
		/**规则组1*/
		/**规则组1--规则1*/
		MethodLeftPart leftPart = BizUtils.buildMethodLeftPart("methodTest", "evalTest", leftPartParameter);
		Criteria criteria1 = Criteria.instance()
				.setLeft(Left.instance(leftPart))
				.setOp(Op.Equals)
				.setValue(SimpleValue.instance("true"));

		/**规则组1--规则2*/

		Parameter a = BizUtils.buildSimpleParameter("a", Datatype.Integer, "3");
		Parameter b = BizUtils.buildSimpleParameter("b", Datatype.Integer, "2");
		MethodLeftPart leftPart2 = BizUtils.buildMethodLeftPart("methodTest", "ltZero", a,b);
		Criteria criteria2 = Criteria.instance()
				.setLeft(Left.instance(leftPart2))
				.setOp(Op.Equals)
				.setValue(SimpleValue.instance("true"));
		Or and = Or.instance().addCriterion(false, criteria1, criteria2);
		/**规则组2*/
		/**规则组2 中的规则1*/
		Parameter orCriteria1Parameter = BizUtils.buildSimpleParameter("hello", Datatype.String, "hello2");
		MethodLeftPart orCriteriaLeftPart1 = BizUtils.buildMethodLeftPart("methodTest", "hello", orCriteria1Parameter);
		Criteria orCriteria1 = Criteria.instance()
				.setLeft(Left.instance(orCriteriaLeftPart1))
				.setOp(Op.Equals)
				.setValue(SimpleValue.instance("hello2"));
		/**规则组2 中的规则2*/

		/*
		//todo 复杂对象场景
		List<String> customers = new ArrayList<String>(){{
			add("123");
			add("546");
		}};
		Map<String,DefaultUser> maps = new HashMap<String,DefaultUser>(){{
			DefaultUser defaultUser = new DefaultUser();
			defaultUser.setAdmin(true);
			defaultUser.setCompanyId("33454");
			put("001",defaultUser);
		}};

		Parameter orCriteria2Parameter = BizUtils.buildComplexObjectValueParameter("customers", Datatype.List,customers);
		Parameter orCriteria3Parameter = BizUtils.buildComplexObjectValueParameter("maps", Datatype.Map,maps);
		MethodLeftPart orCriteriaLeftPart2 = BizUtils.buildMethodLeftPart("methodTest", "printUsers", orCriteria2Parameter,orCriteria3Parameter);
*/
		Parameter orCriteria2Parameter = BizUtils.buildSimpleParameter("username", Datatype.String, "hello3");
		MethodLeftPart orCriteriaLeftPart2 = BizUtils.buildMethodLeftPart("methodTest", "hello", orCriteria2Parameter);
		Criteria orCriteria2 = Criteria.instance()
				.setLeft(Left.instance(orCriteriaLeftPart2))
				.setOp(Op.Equals)
				.setValue(SimpleValue.instance("hello2"));
		Or or = Or.instance().addCriterion(false, orCriteria1, orCriteria2);
		Lhs lhs = Lhs.instance().setCriterion(Or.instance().addCriterion(true, and, or));

		Rhs rhs = Rhs.instance();
		rhs.addAction(BizUtils.buildVariableAssignAction("flag", Datatype.Boolean, "true"));

		Other other = new Other();
		other.addAction(BizUtils.buildVariableAssignAction("flag", Datatype.Boolean, "false"));

		List<Variable> variables = new ArrayList<>();
		Variable variable = new Variable();
		variable.setType(Datatype.String);
		variable.setLabel("username");
		variable.setName("username");
		variables.add(variable);


		Variable variable2 = new Variable();
		variable2.setType(Datatype.String);
		variable2.setLabel("hello");
		variable2.setName("hello");
		variables.add(variable2);
		//ExecutionResponse execute = knowledgeHelper.execute("6123:1:-1",lhs, variables);


		List<VariableLibrary> variableCategoryLibs = new ArrayList<VariableLibrary>();
		if(!CollectionUtils.isEmpty(variables)) {
			//依赖的变量
			VariableLibrary variableLibrary = new VariableLibrary();
			//依赖的变量->变量类型，只支持map结构
			List<VariableCategory> variableCategories = new ArrayList<>();
			VariableCategory variableCategory = new VariableCategory();
			variableCategory.setClazz("java.util.HashMap");
			variableCategory.setName("参数");
			variableCategory.setType(CategoryType.Clazz);
			//依赖的变量->变量信息
			variableCategory.setVariables(variables);
			variableCategories.add(variableCategory);

			//todo
			/*VariableCategory variableCategory2 = new VariableCategory();
			variableCategory2.setClazz("java.util.List");
			variableCategory2.setName("参数");//list参数
			variableCategory2.setType(CategoryType.Clazz);
			//依赖的变量->变量信息
			List<Variable> variables2 = new ArrayList<>();
			Variable variable3 = new Variable();
			variable3.setType(Datatype.List);
			variable3.setLabel("customers");
			variable3.setName("customers");
			variables2.add(variable3);
			variableCategory2.setVariables(variables2);
			variableCategories.add(variableCategory2);*/

			variableLibrary.setVariableCategories(variableCategories);
			variableCategoryLibs.add(variableLibrary);
		}




		ExecutionResponse execute = knowledgeHelper.execute("6123:1:-1",lhs, other, rhs, null);
		ExecutionResponseImpl res=(ExecutionResponseImpl)execute;
		List<RuleInfo> firedRules=res.getFiredRules();
		List<RuleInfo> matchedRules=res.getMatchedRules();
		System.out.println();
	}

	/**
	 * 原始请求地址
	 *
	 * @param req
	 * @param resp
	 * @throws Exception
	 */
	@SuppressWarnings({ "unchecked"})
	public void doTest_back(HttpServletRequest req, HttpServletResponse resp) throws Exception {
		String data=req.getParameter("data");
		ObjectMapper mapper=new ObjectMapper();
		List<Map<String,Object>> list=mapper.readValue(data, ArrayList.class);
		List<VariableCategory> variableCategories=mapToVariableCategories(list);
		Map<VariableCategory,Object> facts=new HashMap<VariableCategory,Object>();
		for(VariableCategory vc:variableCategories){
			String clazz=vc.getClazz();
			Object entity=null;
			if(vc.getName().equals(VariableCategory.PARAM_CATEGORY)){
				entity=new HashMap<String,Object>();
			}else{
				entity=new GeneralEntity(clazz);
			}
			for(Variable var:vc.getVariables()){
				buildObject(entity, var);				
			}
			facts.put(vc,entity);
		}
		String flowId=req.getParameter("flowId");
		long start=System.currentTimeMillis();
		KnowledgeBase knowledgeBase= null;//(KnowledgeBase)httpSessionKnowledgeCache.get(req, KB_KEY);
		if(knowledgeBase==null){
			knowledgeBase= buildKnowledgeBase(req);
			//knowledgeBase = buildKnowledgeBaseByRuleXml(req, TEST_RULR_XML_V2);
		}
		KnowledgePackage knowledgePackage=knowledgeBase.getKnowledgePackage();
		KnowledgeSession session=KnowledgeSessionFactory.newKnowledgeSession(knowledgePackage);
		Map<String,Object> parameters=null;
		for(Object obj:facts.values()){
			if(!(obj instanceof GeneralEntity) && (obj instanceof HashMap)){
				parameters=(Map<String,Object>)obj;
			}else{
				session.insert(obj);				
			}
		}
		ExecutionResponse response=null;
		if(StringUtils.isNotEmpty(flowId)){
			if(parameters!=null){
				response=session.startProcess(flowId,parameters);
			}else{
				response=session.startProcess(flowId);
			}
		}else{
			if(parameters==null){
				response=session.fireRules();			
			}else{
				response=session.fireRules(parameters);						
			}
		}
		for(VariableCategory vc:facts.keySet()){
			Object obj=facts.get(vc);
			if(obj==null){
				continue;
			}
			if(obj instanceof Map && !(obj instanceof GeneralEntity)){
				obj=session.getParameters();
			}
			for(Variable var:vc.getVariables()){
				buildVariableValue(obj, var);
			}
		}
		long end=System.currentTimeMillis();
		long elapse=end-start;
		session.writeLogFile();
		ExecutionResponseImpl res=(ExecutionResponseImpl)response;
		List<RuleInfo> firedRules=res.getFiredRules();
		List<RuleInfo> matchedRules=res.getMatchedRules();
		StringBuffer sb=new StringBuffer();
		sb.append("耗时："+elapse+"ms");
		if(StringUtils.isEmpty(flowId)){
			sb.append("，");
			sb.append("匹配的规则共"+matchedRules.size()+"个");
			if(matchedRules.size()>0){
				buildRulesName(matchedRules, sb);				
			}
			sb.append("；");
			sb.append("触发的规则共"+firedRules.size()+"个");
			buildRulesName(firedRules, sb);
		}
		Map<String,Object> resultMap=new HashMap<String,Object>();
		resultMap.put("info", sb.toString());
		resultMap.put("data", variableCategories);
		writeObjectToJson(resp, resultMap);
	}
	
	private void buildObject(Object obj,Variable var){
		String name=var.getName();
		if(name.indexOf(".")!=-1){
			instanceChildObject(obj,name);
		}
		String defaultValue=var.getDefaultValue();
		if(StringUtils.isBlank(defaultValue)){
			return;
		}
		Datatype type=var.getType();
		if(type.equals(Datatype.List)){
			Utils.setObjectProperty(obj, name, buildList(defaultValue));
		}else if(type.equals(Datatype.Set)){
			Utils.setObjectProperty(obj, name, buildSet(defaultValue));
		}else if(type.equals(Datatype.Map)){
			return;
		}else{
			Object value=type.convert(defaultValue);
			Utils.setObjectProperty(obj, name, value);
		}
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private List<GeneralEntity> buildList(String value){
		try {
			List<GeneralEntity> result=new ArrayList<GeneralEntity>();
			ObjectMapper mapper=new ObjectMapper();
			Map<String,Object> map=mapper.readValue(value, HashMap.class);
			if(map.containsKey("rows")){
				List<Object> list=(List<Object>)map.get("rows");
				for(Object obj:list){
					if(obj instanceof Map){
						GeneralEntity entity=new GeneralEntity((String)map.get("type"));
						entity.putAll((Map)obj);
						result.add(entity);
					}
				}
				return result;
			}else{
				return null;
			}
		} catch (Exception e) {
			throw new RuleException(e);
		}
	}
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private Set<GeneralEntity> buildSet(String value){
		try {
			Set<GeneralEntity> result=new HashSet<GeneralEntity>();
			ObjectMapper mapper=new ObjectMapper();
			Map<String,Object> map=mapper.readValue(value, HashMap.class);
			if(map.containsKey("rows")){
				List<Object> list=(List<Object>)map.get("rows");
				for(Object obj:list){
					if(obj instanceof Map){
						GeneralEntity entity=new GeneralEntity((String)map.get("type"));
						entity.putAll((Map)obj);
						result.add(entity);
					}
				}
				return result;
			}else{
				return null;
			}
		} catch (Exception e) {
			throw new RuleException(e);
		}
	}
	
	private void instanceChildObject(Object obj,String propertyName){
		int pointIndex=propertyName.indexOf(".");
		if(pointIndex==-1){
			return;
		}
		String name=propertyName.substring(0,pointIndex);
		propertyName=propertyName.substring(pointIndex+1);
		try {
			Object instance=PropertyUtils.getProperty(obj, name);
			if(instance!=null){
				instanceChildObject(instance,propertyName);
				return;
			}
			Object targetEntity=new GeneralEntity(name);
			PropertyUtils.setProperty(obj, name, targetEntity);
			instanceChildObject(targetEntity,propertyName);
		} catch (Exception e) {
			throw new RuleException(e);
		}
	}
	

	private void buildRulesName(List<RuleInfo> firedRules, StringBuffer sb) {
		sb.append("：");
		int i=0;
		for(RuleInfo rule:firedRules){
			if(i>0){
				sb.append("，");
			}
			sb.append(rule.getName());
			i++;
		}
	}
	
	private void buildVariableValue(Object object,Variable var){
		String name=var.getName();
		Object value=Utils.getObjectProperty(object, name);
		if(value!=null){
			Datatype type=var.getType();
			if(type.equals(Datatype.List) || type.equals(Datatype.Set)){
				//var.setDefaultValue(value.toString());								
			}else{
				String str=type.convertObjectToString(value);
				var.setDefaultValue(str);				
			}
		}
	}

	public void setRepositoryService(RepositoryService repositoryService) {
		this.repositoryService = repositoryService;
	}
	
	public void setKnowledgeBuilder(KnowledgeBuilder knowledgeBuilder) {
		this.knowledgeBuilder = knowledgeBuilder;
	}
	public void setHttpSessionKnowledgeCache(
			HttpSessionKnowledgeCache httpSessionKnowledgeCache) {
		this.httpSessionKnowledgeCache = httpSessionKnowledgeCache;
	}

	public void setKnowledgeHelper(KnowledgeHelper knowledgeHelper) {
		this.knowledgeHelper = knowledgeHelper;
	}

	@Override
	public String url() {
		return "/packageeditor";
	}

	private final static String TEST_RULR_XML="<?xml version=\"1.0\" encoding=\"UTF-8\"?><rule-set><import-variable-library path=\"jcr:/demo/demo.vl.xml\"/><import-action-library path=\"jcr:/demo/actiondemo.al.xml\"/><remark><![CDATA[]]></remark><rule name=\"rule\" enabled=\"true\"><remark><![CDATA[]]></remark><if><or><atom op=\"GreaterThen\"><left var-category=\"用户\" var=\"age\" var-label=\"年龄\" datatype=\"Integer\" type=\"variable\"></left><value  content=\"10\" type=\"Input\" ></value></atom><atom op=\"Contain\"><left var-category=\"用户\" type=\"variable\"></left><value  content=\"wpx\" type=\"Input\" ></value></atom></or></if><then><console-print><value  content=\"123\" type=\"Input\" ></value></console-print></then><else></else></rule><rule name=\"rule\" enabled=\"true\"><remark><![CDATA[]]></remark><if><and><atom op=\"Equals\"><left var-category=\"用户\" var=\"gender\" var-label=\"性别\" datatype=\"Boolean\" type=\"variable\"></left><value  content=\"true\" type=\"Input\" ></value></atom><atom op=\"Contain\"><left var-category=\"用户\" var=\"mobile\" var-label=\"手机号\" datatype=\"String\" type=\"variable\"></left><value  content=\"153\" type=\"Input\" ></value></atom></and></if><then><console-print><value  content=\"2132134\" type=\"Input\" ></value></console-print></then><else></else></rule></rule-set>";
	private final static String TEST_RULR_XML_V2="<?xml version=\"1.0\" encoding=\"UTF-8\"?><rule-set><remark><![CDATA[]]></remark><rule name=\"rule\"><remark><![CDATA[]]></remark><if><and><atom op=\"GreaterThen\"><left  var-category=\"参数\" var=\"商品id\" var-label=\"skuId\" datatype=\"Long\" type=\"parameter\"></left><value  content=\"10\" type=\"Input\" ></value></atom><or><atom op=\"Contain\"><left  var-category=\"参数\" var=\"商品名称\" var-label=\"skuName\" datatype=\"String\" type=\"parameter\"></left><value  content=\"测试\" type=\"Input\" ></value></atom></or></and></if><then><console-print><value  content=\"21321\" type=\"Input\" ></value></console-print></then><else></else></rule></rule-set>";
}
