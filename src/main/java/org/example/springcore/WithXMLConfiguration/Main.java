package org.example.springcore.WithXMLConfiguration;

import org.springframework.context.support.ClassPathXmlApplicationContext;

class Main {
    static void main() {
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("applicationContext.xml");
        Store bean = (Store) context.getBean("storeSI");
        Store bean2 = (Store) context.getBean("storeCI");
        bean.print();
        bean2.print();
    }
}