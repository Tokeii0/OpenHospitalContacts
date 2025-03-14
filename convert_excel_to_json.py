import pandas as pd
import json
import random

# 读取Excel文件，不使用第一行作为列名
df = pd.read_excel('dhb.xlsx', header=None)

# 根据Excel内容，手动设置列名
df.columns = ['姓名', '电话', '科室']

# 检查数据结构
print("Excel文件列名:", df.columns.tolist())
print("数据行数:", len(df))

# 创建员工列表
employees = []

# 生成随机电话号码（办公电话）
def generate_random_office_phone():
    return f"0712-{random.randint(1000000, 9999999)}"

# 遍历Excel数据，跳过第一行（因为它是我们手动设置的列名）
for i, row in df.iloc[1:].iterrows():
    name = row['姓名']
    department = row['科室']
    mobile_phone = row['电话']
    
    # 处理电话号码，如果是科学计数法格式，则转换为标准格式
    if isinstance(mobile_phone, float):
        # 确保电话号码为11位
        mobile_phone = f"{mobile_phone:.0f}"
        if len(mobile_phone) > 11:
            mobile_phone = mobile_phone[:11]
    
    # 如果姓名或科室为空，跳过此行
    if pd.isna(name) or pd.isna(department) or not str(name).strip() or not str(department).strip():
        continue
    
    # 创建员工对象
    employee = {
        "name": str(name).strip(),
        "department": str(department).strip(),
        "position": "职工",  # 默认职位为"职工"
        "officePhone": generate_random_office_phone(),
        "mobilePhone": str(mobile_phone).strip()
    }
    
    employees.append(employee)

# 保存为JSON文件
with open('app/src/main/assets/employees.json', 'w', encoding='utf-8') as f:
    json.dump(employees, f, ensure_ascii=False, indent=2)

print(f"成功转换 {len(employees)} 条员工记录到JSON文件")
print("JSON文件已保存到: app/src/main/assets/employees.json")

# 打印前5条记录作为示例
print("\n前5条记录示例:")
for i, emp in enumerate(employees[:5]):
    print(f"{i+1}. {emp}")

# 打印JSON文件的前几行内容
print("\nJSON文件内容预览:")
with open('app/src/main/assets/employees.json', 'r', encoding='utf-8') as f:
    json_content = f.read(500)  # 读取前500个字符
    print(json_content + "...")
