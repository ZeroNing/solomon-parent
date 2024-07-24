package com.steven.solomon.sort;


import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

/**
 * 鸡尾酒排序
 */
public class CocktailSortService implements SortService {

    @Override
    public <T> Collection<T> sort(Collection<T> list, Comparator<? super T> comparator) {

        List<T>  dataList  =  new  ArrayList<>(list);  //  将Collection转换为List，因为List支持通过索引访问
        boolean  swapped;
        int  start  =  0;
        int  end  =  dataList.size()  -  1;

        do  {
            swapped  =  false;

            //  正向遍历，找到最大元素
            for  (int  i  =  start;  i  <  end;  ++i)  {
                if  (comparator.compare(dataList.get(i),  dataList.get(i  +  1))  >  0)  {
                    T  temp  =  dataList.get(i);
                    dataList.set(i,  dataList.get(i  +  1));
                    dataList.set(i  +  1,  temp);
                    swapped  =  true;
                }
            }

            if  (!swapped)  {
                break;
            }

            swapped  =  false;
            end--;

            //  反向遍历，找到最小元素
            for  (int  i  =  end  -  1;  i  >=  start;  --i)  {
                if  (comparator.compare(dataList.get(i),  dataList.get(i  +  1))  >  0)  {
                    T  temp  =  dataList.get(i);
                    dataList.set(i,  dataList.get(i  +  1));
                    dataList.set(i  +  1,  temp);
                    swapped  =  true;
                }
            }

            start++;

        }  while  (swapped);

        return  dataList;
    }
}
