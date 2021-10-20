package com.lishiliang.model;

public enum ErrorCode {
	
	DD_EXIST("110151011","数据字典已经存在","数据字典已经存在"),
	ERRCODE_EXIST("110151012","错误码已经存在","错误码已经存在"),
	QUERY_ERROR_CODE_EXCEPTION("110151009","错误码查询异常","错误码查询异常"),
	GET_ERRCODE_LIST_ERROR("110151003","查询错误码列表异常","系统异常"),
	GET_ERRCODE_PAGE_ERROR("110151001","获取错误码分页数据异常","系统异常"),
	GET_TOTAL_COUNT_ERROR("110151002","获取错误码分页总数据异常","系统异常"),
	GET_ERRCODE_EXCEPTION("110151009","获取错误码记录异常","系统异常"),
	ADD_ERRORCODE_EXCEPTION("110151004","添加错误码异常","系统异常"),
	UPDATE_ERRORCODE_EXCEPTION("110151005","更新错误码异常","系统异常"),
	BATCH_UPDATE_EXCEPTION("110151000","错误码批量导入异常","系统异常"),
	ADD_DD_EXCEPTION("110151008","添加数据字典异常","系统异常"),
	UPDATE_DD_EXCEPTION("110151007","更新数据字典异常","系统异常"),
	DD_BATCH_UPDATE_EXCEPTION("110151013","数据字典批量导入异常","系统异常"),
	ADD_SYSCODE_EXCEPTION("110151014","系统码添加异常","系统异常"),
	UPDATE_SYSCODE_EXCEPTION("110151015","系统码更新异常","系统异常"),
	QUERY_SYSCODE_EXCEPTION("110151016","系统码分页查询异常","系统异常"),
	SYSCODE_GET_TOTAL_SIZE_EXCEPTION("110151017","统计系统码异常","系统异常"),
	GET_ALL_SYSCODE_EXCEPTION("110151018","获取所有系统码异常","系统异常"),
	SYSCODE_EXIST("110151019","系统码已经存在","系统异常"),
	PROJCODE_MAPED_TEMPLATEID_NOT_CONFIGURE("110151020","没有配置系统码对应的billno模板号","没有配置系统码对应的billno模板号"),
	KMI_AES_ENCRYPT_EXCEPTION("110160012","数据加密失败","数据加密失败"),
	KMI_AES_DECRYPT_EXCEPTION("110160013","数据解密失败","数据解密失败"),
	
	CODE_1001("110061001", "元数据不存在","元数据不存在"), 
	CODE_1002("110061002", "元数据状态无效", "元数据状态无效"), 
	CODE_1003("110061003", "流水号已用完", "流水号已用完"), 
	CODE_1004("110061004", "请求时间有误", "请求时间有误"), 
	CODE_1005("110061005", "系统运行异常", "系统运行异常"), 
	CODE_1006("110061006", "初始化元数据实例表数据失败", "初始化元数据实例表数据失败"), 
	CODE_1007("110061007", "获取数据库时间失败!", "获取数据库时间失败!"), 
	CODE_1008("110061008", "从定义表中获取总记录数失败!", "从定义表中获取总记录数失败!"), 
	CODE_1009("110061009", "web页面参数传递有误!", "web页面参数传递有误!"), 
	CODE_1010("110061010", "SQL执行失败!", "SQL执行失败!"), 
	CODE_1011("110061011", "参数有误!", "参数有误!"), 
	CODE_1012("110061012", "数据库中无相关记录!", "数据库中无相关记录!"), 
	CODE_WEB_1013("110061013", "保存元数据失败了", "保存元数据失败了"), 
	CODE_1014_UNKNOWN_ERROR("110061014", "未知异常", "未知异常");
	
	
	private String errCode;
	
	private String innerDesc;
	
	private String outDesc;
	
	ErrorCode(String errCode,String innerDesc,String outDesc) {
		this.errCode=errCode;
		this.outDesc=outDesc;
		this.innerDesc=innerDesc;
	}
	public String getErrCode() {
		return errCode;
	}
	public String getInnerDesc() {
		return innerDesc;
	}

	public String getOutDesc() {
		return outDesc;
	}
}
