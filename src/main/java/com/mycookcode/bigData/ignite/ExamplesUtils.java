package com.mycookcode.bigData.ignite;

import org.apache.ignite.Ignite;
import org.apache.ignite.cluster.ClusterGroup;

import java.net.URL;
import java.util.List;

/**
 * Created by zhaolu on 2018/2/4.
 */
public class ExamplesUtils {

    private static final ClassLoader CLS_LDR = ExamplesUtils.class.getClassLoader();


    /**
     * 检查最小内存阀值
     * @param min
     */
    public static void checkMinMemory(long min) {
        long maxMem = Runtime.getRuntime().maxMemory();

        if (maxMem < .85 * min) {
            System.err.println("Heap limit is too low (" + (maxMem / (1024 * 1024)) +
                    "MB), please increase heap size at least up to " + (min / (1024 * 1024)) + "MB.");

            System.exit(-1);
        }
    }


    public static URL url(String path) {
        URL url = CLS_LDR.getResource(path);

        if (url == null)
            throw new RuntimeException("Failed to resolve resource URL by path: " + path);

        return url;
    }


    /**
     * 检查最小topology节点数量大小
     *
     * @param grp
     * @param size
     * @return
     */
    public static boolean checkMinTopologySize(ClusterGroup grp, int size) {
        int prjSize = grp.nodes().size();

        if (prjSize < size) {
            System.err.println(">>> Please start at least " + size + " cluster nodes to run example.");

            return false;
        }

        return true;
    }

    /**
     * 检查是否集群服务节点
     * @param ignite
     * @return
     */
    public static boolean hasServerNodes(Ignite ignite) {
        if (ignite.cluster().forServers().nodes().isEmpty()) {
            System.err.println("Server nodes not found (start data nodes with ExampleNodeStartup class)");

            return false;
        }

        return true;
    }

    /**
     * 控制台打印输出查询结果
     * @param res
     */
    public static void printQueryResults(List<?> res) {
        if (res == null || res.isEmpty())
            System.out.println("Query result set is empty.");
        else {
            for (Object row : res) {
                if (row instanceof List) {
                    System.out.print("(");

                    List<?> l = (List)row;

                    for (int i = 0; i < l.size(); i++) {
                        Object o = l.get(i);

                        if (o instanceof Double || o instanceof Float)
                            System.out.printf("%.2f", o);
                        else
                            System.out.print(l.get(i));

                        if (i + 1 != l.size())
                            System.out.print(',');
                    }

                    System.out.println(')');
                }
                else
                    System.out.println("  " + row);
            }
        }
    }

}
