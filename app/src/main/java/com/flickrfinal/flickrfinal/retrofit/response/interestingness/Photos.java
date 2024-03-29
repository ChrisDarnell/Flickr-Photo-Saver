
package com.flickrfinal.flickrfinal.retrofit.response.interestingness;

import java.util.ArrayList;
import java.util.List;
import com.google.gson.annotations.Expose;

public class Photos {

    @Expose
    private int page;
    @Expose
    private int pages;
    @Expose
    private int perpage;
    @Expose
    private String total;
    @Expose
    private List<Photo> photo = new ArrayList<Photo>();

    /**
     * 
     * @return
     *     The page
     */
    public int getPage() {
        return page;
    }

    /**
     * 
     * @param page
     *     The page
     */
    public void setPage(int page) {
        this.page = page;
    }

    /**
     * 
     * @return
     *     The pages
     */
    public int getPages() {
        return pages;
    }

    /**
     * 
     * @param pages
     *     The pages
     */
    public void setPages(int pages) {
        this.pages = pages;
    }

    /**
     * 
     * @return
     *     The perpage
     */
    public int getPerpage() {
        return perpage;
    }

    /**
     * 
     * @param perpage
     *     The perpage
     */
    public void setPerpage(int perpage) {
        this.perpage = perpage;
    }

    /**
     * 
     * @return
     *     The total
     */
    public String getTotal() {
        return total;
    }

    /**
     * 
     * @param total
     *     The total
     */
    public void setTotal(String total) {
        this.total = total;
    }

    /**
     * 
     * @return
     *     The photo
     */
    public List<Photo> getPhoto() {
        return photo;
    }

    /**
     * 
     * @param photo
     *     The photo
     */
    public void setPhoto(List<Photo> photo) {
        this.photo = photo;
    }

}
