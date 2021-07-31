package com.jeesuite.common.model;

import java.util.ArrayList;
import java.util.List;

public class Page<T> extends PageParams{

    //总记录数
    private long total;
    //总页数
    private int pages;
    //结果集
    private List<T> data;
     
	public Page() {}
	
	public Page(int pageNo,int pageSize,long total, List<T> data) {
		setPageNo(pageNo);
		setPageSize(pageSize);
		this.total = total;
		this.data = data;
		this.pages = (int) ((this.total / this.getPageSize()) + (this.total % this.getPageSize() == 0 ? 0 : 1));
	}
	
	public Page(PageParams pageParams,long total, List<T> data) {
		setPageNo(pageParams.getPageNo());
		setPageSize(pageParams.getPageSize());
		this.total = total;
		this.data = data;
		this.pages = (int) ((this.total / this.getPageSize()) + (this.total % this.getPageSize() == 0 ? 0 : 1));
	}

    public static <T> Page<T> blankPage(int pageNo,int pageSize){
    	Page<T> page = new Page<>();
    	page.setPageNo(pageNo);
    	page.setPageSize(pageSize);
		page.total = 0;
		page.data = new ArrayList<>(0);
		page.setPages(0);
    	return page;
    }

	public long getTotal() {
		return total;
	}
	public void setTotal(long total) {
		this.total = total;
	}
	public int getPages() {
		return pages;
	}
	public void setPages(int pages) {
		this.pages = pages;
	}
	public List<T> getData() {
		return data == null ? (data = new ArrayList<>()) : data;
	}
	public void setData(List<T> data) {
		this.data = data;
	}
    
    
}
