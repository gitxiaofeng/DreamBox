from lxml import etree
from common import RawInput
import re
import logging
import constant

PROGUARD_KEYS = 'abcdefghigklmnopqrstuvwxyz' + 'abcdefghigklmnopqrstuvwxyz'.upper()

VIEW_TAGS = [
    'view',
    'text',
    'button',
    'image',
    'progress',
    'loading',
    'list',
    'flow',
    'header',
    'footer',
    'vh'
]

VIEW_PARENT = [
    'list',
    'flow',
    'header',
    'footer',
    'vh'
]

IDREF_ATTRS = [
    'leftToLeft',
    'leftToRight',
    'rightToRight',
    'rightToLeft',
    'topToTop',
    'topToBottom',
    'bottomToTop',
    'bottomToBottom'
]

# 以下标签是对动作标签做包裹的标签声明
CALLBACK_AND_ALIAS_TAGS = [
    'onVisible',
    'onInvisible',
    'onClick',
    'actionAlias',
    'onSuccess',
    'onError',
    'onPositive',
    'onNegative'
]

V2_NEW_TAGS = [
    'list', 'flow', 'progress'
]


class Node:
    def __init__(self, tag, parent=None) -> None:
        self.tag = tag
        self.parent = parent
        self.attrs = {}
        self.children = []

    def __str__(self):
        return 'Node -{tag}-, attr-count:{count}, children:{children}'.format(tag=self.tag,
                                                                              count=len(self.attrs),
                                                                              children=self.children)


class RuleChecker:
    """外部可以实现此结构用来做对于node tree的检测"""

    def on_tree_generated(self, root: Node):
        pass


class _Converter:
    """接受外部传入的xml数据，以及周边的配置数据，输出是object，可以直接被json化"""

    def __init__(self, xml_str: str, cfg: RawInput, checker: RuleChecker = None) -> None:
        super().__init__()
        # 原始XML string
        self.raw_xml = xml_str
        # 用户输入配置
        self.raw_cfg = cfg
        # 哪些节点的tag可以被视为是view
        self.view_tags = VIEW_TAGS
        self._expand_by_ext_cfg(self.view_tags, 'ext', 'view_tags')
        # 哪些节点的tag可以被视为是view parent
        self.view_parent_tags = VIEW_PARENT
        self._expand_by_ext_cfg(self.view_parent_tags, 'ext', 'view_parent_tags')
        # 哪些属性可以被视作是对ID的引用
        self.idref_attrs = IDREF_ATTRS
        self._expand_by_ext_cfg(self.idref_attrs, 'ext', 'idref_attr_names')
        self.callback_and_alias_tags = CALLBACK_AND_ALIAS_TAGS
        self._expand_by_ext_cfg(self.callback_and_alias_tags, 'ext', 'callback_tags')
        # 自动转化后的成为int类型的ID
        self.int_ids = {}
        # dsl的树状结构根节点
        self.tree_root = None
        # 声明了哪些ID
        self.declared_ids = None
        # 引用了哪些ID
        self.ref_ids = None
        # 总共有多少属性key
        self.attr_keys = None
        # 生成的混淆key集合
        self.proguard_keys = None
        # 可能存在的外部规则检测器
        self.rule_checker = checker

    def _expand_by_ext_cfg(self, target, *cfg_source):
        if self.raw_cfg.ext_cfg is None:
            return
        if type(target) is not list:
            raise Exception('所扩展的属性本身必须是list，请检查代码')
        parent_level = self.raw_cfg.ext_cfg[cfg_source[0]]
        if parent_level:
            child_level = None
            try:
                child_level = parent_level[cfg_source[1]]
            except KeyError:
                pass
            if child_level:
                values = child_level.split(',')
                if values:
                    for v in values:
                        striped_tag = v.strip()
                        if striped_tag not in target:
                            target.append(striped_tag)

    def _wrap_keys(self, key):
        if self.proguard_keys:
            return self.proguard_keys[key]
        else:
            return key

    def convert(self):
        # 首先，确认下使用者是否强制了适配v1版本DSL
        if self.raw_cfg.force_compat_v1:
            constant.FORCE_TARGET = constant.RUNTIME_VER_1
        self._xml()
        self._make_id_as_int()
        self._try_gen_proguard_map()
        return self._transform()

    def _make_id_as_int(self):
        if len(self.declared_ids) > 65535:
            raise Exception('过多ID使用，超过65535，请确认必要性并修正')
        for index in range(len(self.declared_ids)):
            # 根据统计到的ID自动将其转换为int，0~65535
            # parent一定一直是0
            id_ = self.declared_ids[index]
            self.int_ids[id_] = str(index)

    def _transform(self):
        # 生成json树（dict）
        root_obj = self._gen_node(self.tree_root)
        # 置换view ID，str -> int
        root_obj = self._swap_ids(root_obj)
        proguard_map = None
        # 如果需要混淆，那么两件事情：1替换所有的节点tag和attr key 2填充混淆map json字段
        if self.proguard_keys:
            logging.debug('开始混淆...')
            # 开始混淆替换dict
            root_obj = self._proguard_tree(root_obj)
            # 翻转混淆key，便于runtime处理
            proguard_map = self._gen_proguard_json_map()
            logging.debug('混淆完成')
        obj = {'dbl': root_obj}
        if proguard_map is not None:
            obj.update(proguard_map)
        return obj

    def _gen_proguard_json_map(self):
        if self.proguard_keys is None:
            return None
        reversed_map = {}
        for k, v in self.proguard_keys.items():
            reversed_map[v] = k
        return {'map': reversed_map}

    def _gen_node(self, n: Node):
        json_dict = {}
        if n.tag == 'render':
            # render下所有节点都放到数组中
            json_dict = []
        for k, v in n.attrs.items():
            # 属性赋值
            json_dict[k] = v
        if n.parent is not None and n.parent.tag != 'meta':
            # meta节点下可以任意的声明节点，都会被自动转换成对应的json，除此之外要经过特殊类型节点的检查及转化
            if n.tag in self.view_parent_tags:
                # 对于view parent节点，其一定会内置children节点，内部包括子view
                json_dict['children'] = []
            if n.tag in self.callback_and_alias_tags:
                # 对于callback以及alias标签，只在v2 DSL上对其内部内置actions字段，用于收敛多个action
                if constant.FORCE_TARGET is not constant.RUNTIME_VER_1:
                    json_dict['actions'] = []
            if n.tag in self.view_tags:
                # 视图节点的类型放入json-object内部，type下
                json_dict['type'] = n.tag
        for child in n.children:
            if type(json_dict) is dict:
                if 'children' in json_dict.keys():
                    json_dict['children'].append(self._gen_node(child))
                    continue
                if 'actions' in json_dict.keys():
                    json_dict['actions'].append({
                        child.tag: self._gen_node(child)
                    })
                    continue
                if child.tag in json_dict:
                    # 如果是已经存在的节点，那么转成数组
                    exist = json_dict[child.tag]
                    if type(exist) is list:
                        # 应该第二次才会走到这里，此时已经存在的已经是数组
                        exist.append(self._gen_node(child))
                        json_dict[child.tag] = exist
                    else:
                        # 一个重复的节点进入时第一次走到这里，将已有的json-dict转换为array
                        child_list = [exist, self._gen_node(child)]
                        json_dict[child.tag] = child_list
                else:
                    # 新节点直接生成
                    json_dict[child.tag] = self._gen_node(child)
            if type(json_dict) is list:
                json_dict.append(self._gen_node(child))
        return json_dict

    def _swap_ids(self, node, tag=None):
        if node is None:
            return None
        if tag is not None and tag in self.view_tags:
            # 这里才是真正的替换代码所在，只对view节点进行替换
            if type(node) is not dict:
                raise Exception('替换ID出错')
            for k, v in node.copy().items():
                if k == 'id' or k in IDREF_ATTRS:
                    new_value = self.int_ids[v]
                    del node[k]
                    node[k] = new_value
            if tag in self.view_parent_tags and 'children' in node:
                children = node['children']
                del node['children']
                node['children'] = []
                for c in children:
                    try:
                        node['children'].append(self._swap_ids(c, c['type']))
                    except KeyError:
                        raise Exception('出现未知view标签，请检查DSL书写是否正确以及命令行配置是否已齐备')
            return node
        if type(node) is dict:
            for k, v in node.copy().items():
                node[k] = self._swap_ids(v, k)
        if type(node) is list and tag == 'render':
            new_node = []
            for child_node in node:
                new_node.append(self._swap_ids(child_node, child_node['type']))
            return new_node
        return node

    def _proguard_tree(self, tree):
        if type(tree) is not dict:
            raise Exception('编译错误，产出类型非tree')
        if self.proguard_keys is None:
            return tree
        for key, value in tree.copy().items():
            after_key, after_value = key, value
            if key in self.proguard_keys.keys():
                after_key = self.proguard_keys[key]
            if type(value) is dict:
                after_value = self._proguard_tree(value)
            if type(value) is list:
                after_value = []
                for child_value in value.copy():
                    if type(child_value) is dict:
                        after_value.append(self._proguard_tree(child_value))
            if after_key != key:
                tree[after_key] = after_value
                del tree[key]
        return tree

    def _xml(self):
        # 构造XML解析器
        parser = etree.XMLParser(ns_clean=True,
                                 remove_comments=True,
                                 no_network=True,
                                 load_dtd=False,
                                 huge_tree=True)
        xml_root = etree.fromstring(self.raw_xml, parser=parser)
        # 关于etree库解析出的xml的基本说明
        # node.tag 节点名称
        # node.text 节点内容 <xxx>123</xxx> 123是内容
        # node.attrib 节点属性
        # 我们最终在意的就是Node类中所定义的数据
        # 1. 首先，构造一个虚拟根节点
        root = Node('root')

        # 当做栈使用，用来按顺序保存解析出来的节点，后续还能够按照顺序转化为json
        # 理解上雷同N叉数利用栈存储以保障顺序
        tag_pointer = [root]
        # 用来存储元数据的数组
        meta_keys = []
        # 存储src、srcMock属性的数据属性数组集合
        src_values = []
        # 存储所有扫描出来的属性名称，作为后续混淆的输入
        attribute_keys = ['type', ]
        # 存储所有扫描出来的声明的ID
        declare_ids = ['parent', ]
        # 存储所有扫描出来的被引用的ID
        reference_ids = ['parent', ]
        # 存储所有的节点tag
        element_tags = []

        # 扫描整个XML，收集必要数据
        for node_type, raw_node in etree.iterwalk(xml_root, events=('start', 'end')):
            # xml中可以有-，但是json中不可以，所以默认都换成下划线_（为了适配iOS的json解析）
            # 节点名称，如render、list等，<[节点名称]>
            element_tag = raw_node.tag.replace('-', '_')
            if node_type == 'start':
                n = Node(element_tag)
                if element_tag not in element_tags:
                    element_tags.append(element_tag)
                for attribute_key in raw_node.attrib.keys():
                    attribute_value = raw_node.attrib[attribute_key]
                    if 'noNamespaceSchemaLocation' in attribute_key:
                        # 忽略XSD
                        continue
                    if n.tag == 'meta':
                        if '-' in attribute_key:
                            raise Exception('meta节点的key不可以包含破折号，请替换为下划线')
                        meta_keys.append(attribute_key)
                    else:
                        if attribute_key not in attribute_keys:
                            attribute_keys.append(attribute_key)
                    if attribute_key == 'srcMock' and self.raw_cfg.checkRule:
                        # 严格模式（release模式）将自动移除srcMock字段
                        continue
                    if attribute_key == 'src' or attribute_key == 'srcMock':
                        src_values.append(attribute_value)
                    if attribute_key == 'id' and element_tag in self.view_tags:
                        if attribute_value == 'parent' or attribute_value == '0':
                            raise Exception('不可以定义内容为parent或0的id值，错误行数: ' + str(raw_node.sourceline))
                        if attribute_value not in declare_ids:
                            declare_ids.append(attribute_value)
                    if attribute_key in self.idref_attrs:
                        # 这个属性后的值的意图是引用其他的ID
                        if attribute_value == '0':
                            raise Exception('请使用parent作为id值，0为内部使用协议，不确定未来可用性，错误行数: ' + str(raw_node.sourceline))
                        if attribute_value not in reference_ids:
                            reference_ids.append(attribute_value)
                    if attribute_key == 'width' or attribute_key == 'height':
                        if attribute_value == 'wrap':
                            # 宽高的默认值直接忽略掉
                            continue
                        if str(attribute_value).isdigit():
                            attribute_value += 'dp'
                    n.attrs[attribute_key] = attribute_value
                current = tag_pointer[-1]
                n.parent = current
                current.children.append(n)
                tag_pointer.append(n)
            if node_type == 'end':
                tag_pointer.pop()

        logging.debug('通过DSL有效性检查')

        # 是否进行严格DSL规则检查
        if self.raw_cfg.checkRule:
            logging.debug('检测数据有效性...')
            for idref in reference_ids:
                if idref not in declare_ids:
                    raise Exception('XML中出现了未知id，请修正: ' + idref)
            for idref in declare_ids:
                if idref not in reference_ids:
                    raise Exception('XML中声明了多余的id，请修正: ' + idref)

            # 检查src里边的书写是否正确
            for src in src_values:
                # 检查，不支持{name:asd}这样的形式
                match = re.match(r'^{([a-zA-z0-9]*:+.*)}$', src)
                if match:
                    raise Exception('并不支持{xx:xx}这样的数据，请检查：' + src)
                # 检查，${xx.xx}这样的数据引用是否合法，若meta中没有又不是pool或ext则报错
                match = re.match(r'^\${([a-zA-Z_]*)[.]?.*}$', src)
                if match:
                    want_key = match.groups()[0]
                    if want_key not in meta_keys and want_key not in ['pool', 'ext']:
                        raise Exception('src/srcMock所引用的变量在meta中不存在: {}'.format(want_key))
            logging.debug('数据有效性检查通过')

        self.tree_root = root.children[0]
        self.declared_ids = declare_ids
        self.ref_ids = reference_ids
        self.attr_keys = attribute_keys

        if self.rule_checker:
            self.rule_checker.on_tree_generated(self.tree_root)

    def _try_gen_proguard_map(self):
        # 如果没要求混淆，则退出
        if not self.raw_cfg.proguard:
            return
        # 目前至多支持两位字符串混淆码
        i, j = 0, -1
        self.proguard_keys = {}
        proguard_key_all_used = False
        for key in self.attr_keys:
            if proguard_key_all_used:
                break
            while i < len(PROGUARD_KEYS) and j < len(PROGUARD_KEYS):
                one = PROGUARD_KEYS[i]
                two = PROGUARD_KEYS[j]
                # only i
                if i < len(PROGUARD_KEYS) - 1 and j == -1:
                    if one not in self.proguard_keys.values():
                        self.proguard_keys[key] = one
                        i += 1
                        break
                    else:
                        i += 1
                    continue
                elif i == len(PROGUARD_KEYS) - 1 and j == -1:
                    i, j = 0, 0
                    continue
                else:
                    combine = ''.join([one, two])
                    if combine not in self.proguard_keys.values():
                        self.proguard_keys[key] = combine
                        break
                    if i < len(PROGUARD_KEYS) - 1:
                        i += 1
                        continue
                    if j < len(PROGUARD_KEYS) - 1:
                        j += 1
                        continue
                    # 到达这里就是已经都打满了
                    proguard_key_all_used = True
                    if self.raw_cfg.proguard:
                        logging.warning('可用混淆key已用尽')
                    break


def convert(xml: str, cfg: RawInput):
    from ext_rule_checker import DBRC
    return _Converter(xml, cfg, DBRC()).convert()
