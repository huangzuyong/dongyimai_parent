package com.offcn.group;

import com.offcn.pojo.TbSpecification;
import com.offcn.pojo.TbSpecificationOption;

import java.io.Serializable;
import java.util.List;

public class Specification implements Serializable {

    public TbSpecification getSpecification() {
        return specification;
    }

    public void setSpecification(TbSpecification specification) {
        this.specification = specification;
    }

    public List<TbSpecificationOption> getSpecificationOptionLIst() {
        return specificationOptionLIst;
    }

    public void setSpecificationOptionLIst(List<TbSpecificationOption> specificationOptionLIst) {
        this.specificationOptionLIst = specificationOptionLIst;
    }

    public Specification(TbSpecification specification, List<TbSpecificationOption> specificationOptionLIst) {
        this.specification = specification;
        this.specificationOptionLIst = specificationOptionLIst;
    }

    private TbSpecification specification;

    private List<TbSpecificationOption> specificationOptionLIst;

    public Specification() {
    }
}
