package com.sforce.soap.partner;

/**
 * Generated by ComplexTypeCodeGenerator.java. Please do not edit.
 */
public interface ISoqlConditionGroup extends com.sforce.soap.partner.ISoqlWhereCondition {

      /**
       * element : conditions of type {urn:partner.soap.sforce.com}SoqlWhereCondition
       * java type: com.sforce.soap.partner.SoqlWhereCondition[]
       */

      public com.sforce.soap.partner.ISoqlWhereCondition[] getConditions();

      public void setConditions(com.sforce.soap.partner.ISoqlWhereCondition[] conditions);

      /**
       * element : conjunction of type {urn:partner.soap.sforce.com}soqlConjunction
       * java type: com.sforce.soap.partner.SoqlConjunction
       */

      public com.sforce.soap.partner.SoqlConjunction getConjunction();

      public void setConjunction(com.sforce.soap.partner.SoqlConjunction conjunction);


}