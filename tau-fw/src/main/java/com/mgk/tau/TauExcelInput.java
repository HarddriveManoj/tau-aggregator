package com.mgk.tau;


import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import static java.lang.annotation.ElementType.METHOD;

@Retention(RetentionPolicy.RUNTIME)
@Target({METHOD})
public @interface TauExcelInput {

    String filename() default "";

    String[] sheetName() default  {};

    String forEach() default "";

    boolean forEachLookup() default false;

    boolean parallel() default false;

    String testCaseGroupName() default "";

    String testCaseNameKey() default "";

    String treeBranchGroupName() default "";

    String dataProvider() default "TauExcelInputProvider";

    int startRow() default 0;

    int startCol() default 0;
}
