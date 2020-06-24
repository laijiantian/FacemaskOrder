package com.facemask.service.impl;

import com.facemask.domain.Facemask;
import com.facemask.domain.Orders;
import com.facemask.domain.Person;
import com.facemask.dto.OrderExecution;
import com.facemask.enums.OrderStateEnum;
import com.facemask.exception.NoNumberException;
import com.facemask.exception.OrderException;
import com.facemask.exception.RepeatOrderException;
import com.facemask.mapper.FacemaskMapper;
import com.facemask.mapper.OrdersMapper;
import com.facemask.service.OrdersService;
import org.mybatis.logging.Logger;
import org.mybatis.logging.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Service
public class OrdersServiceImpl implements OrdersService {
    private final Logger logger= LoggerFactory.getLogger(this.getClass());
    @Autowired
    OrdersMapper ordersMapper;
    @Autowired
    FacemaskMapper facemaskMapper;

    @Override
    public int insertOrders(Orders orders) {
        return ordersMapper.insertOrders(orders);
    }

    @Override
    public Orders quaryOrderBypId(Integer pId) {
        return ordersMapper.quaryOrderBypId(pId);
    }

    @Override
    public Orders quaryOrderByorderId(Integer orderId) {
        return ordersMapper.quaryOrderByorderId(orderId);
    }

    @Override
    public int deleteOrders(int orderId) {
        return ordersMapper.deleteOrders(orderId);
    }

    @Override
    public List<Orders> findAllOrders() {
        return ordersMapper.findAllOrders();
    }

    @Override
    public int updateOrder(Orders orders) {
        return ordersMapper.updateOrder(orders);
    }


    //判断预约条件是否满足，主要判断是否有库存和有没有重复预约
    @Override
    @Transactional
    public OrderExecution order(Orders orders) throws Exception{
        //查看库存是否大于10
        try {
            int inventory = facemaskMapper.findInventoryByFid(orders.getFmaskId());
            System.out.println(inventory);
            if (inventory < 10) {
                throw new NoNumberException("没有库存");
            } else {
                    Orders orders1 = ordersMapper.quaryOrderBypId(orders.getpId());
                if (orders1.getpId()!=null) {
                    //重复预约
                    throw new RepeatOrderException("重复预约");
                } else {
                    int insert = ordersMapper.insertOrders(orders);
                    int subtract = facemaskMapper.subtract_f(orders.getFmaskId());
                    String facemaskName = facemaskMapper.findF_Name(orders.getFmaskId());
                    System.out.println("减库存");
                    return new OrderExecution(facemaskName,OrderStateEnum.SUCCESS);
                }

            }
        } catch (NoNumberException e1) {
            throw e1;
        } catch (RepeatOrderException e2) {
            throw e2;
        }catch (Exception e){
            //所有编译期异常转换为运行期异常
            throw new OrderException("order inner error: "+e.getMessage());
        }
    }

}
