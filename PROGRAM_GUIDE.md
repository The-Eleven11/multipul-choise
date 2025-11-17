# 多选题测验程序说明文档

## 目录
- [1. 程序概述](#1-程序概述)
- [2. 程序运行原理](#2-程序运行原理)
- [3. 架构设计](#3-架构设计)
- [4. 依赖关系详解](#4-依赖关系详解)
- [5. 数据流程](#5-数据流程)
- [6. 核心组件说明](#6-核心组件说明)
- [7. 更新历史](#7-更新历史)
- [8. 技术栈](#8-技术栈)

---

## 1. 程序概述

本程序是一个基于Java Swing的多选题测验应用，专为CSC207 Arcade项目设计。它提供了一个交互式的测验界面，用户可以通过选择答案来测试自己的知识水平。

### 1.1 主要功能
- 加载15道随机题目
- 以图片形式显示题目
- 提供A、B、C、D四个选项按钮
- 即时视觉反馈（正确答案显示绿色，错误答案显示红色）
- 只有选择正确答案后才会自动进入下一题
- 测验结束后显示准确率和总用时

### 1.2 运行方式
```bash
# 使用Maven运行
mvn clean compile exec:java -Dexec.mainClass="com.csc207.arcade.multiplechoice.app.Main"

# 或使用运行脚本
./run.sh
```

---

## 2. 程序运行原理

### 2.1 启动流程

程序的启动过程分为三个主要步骤：

```
1. 数据初始化 (DataInitializer)
   ↓
2. 依赖注入 (Dependency Injection)
   ↓
3. UI启动 (Swing UI Launch)
```

#### 步骤详解：

**步骤1：数据初始化**
- `DataInitializer.run()` 扫描 `src/main/resources/data/images/` 目录
- 解析文件名格式：`{id}_level{难度}_answer{正确答案}.png`
- 生成 `questions.json` 文件，包含所有题目的元数据

**步骤2：依赖注入**
- 创建所有必要的组件实例
- 按照Clean Architecture的原则连接各层组件
- 建立Presenter与ViewModel之间的连接

**步骤3：UI启动**
- 在Swing事件调度线程中启动UI
- 创建QuizView窗口
- 调用 `quizController.startQuiz()` 加载第一题

### 2.2 测验循环

测验的核心循环遵循以下流程：

```
显示题目
   ↓
用户选择答案
   ↓
检查答案正确性
   ├── 正确 → 显示绿色 → 等待1秒 → 加载下一题
   └── 错误 → 显示红色 → 等待用户再次选择
```

---

## 3. 架构设计

本程序严格遵循**Clean Architecture（清晰架构）**原则，这是一种著名的软件设计模式，由Robert C. Martin（Uncle Bob）提出。

### 3.1 架构层次

```
┌─────────────────────────────────────────────┐
│   Frameworks & Drivers (外层)               │
│   - QuizView (Swing UI)                     │
│   - JsonQuestionRepository (数据访问)        │
└──────────────────┬──────────────────────────┘
                   │
┌──────────────────▼──────────────────────────┐
│   Interface Adapters (接口适配层)           │
│   - QuizController                          │
│   - QuizPresenter                           │
│   - QuizViewModel, ResultsViewModel         │
└──────────────────┬──────────────────────────┘
                   │
┌──────────────────▼──────────────────────────┐
│   Use Cases (业务逻辑层)                     │
│   - QuizInteractor                          │
│   - SubmitAnswerInteractor                  │
└──────────────────┬──────────────────────────┘
                   │
┌──────────────────▼──────────────────────────┐
│   Entities (核心实体层)                      │
│   - QuizQuestion                            │
│   - QuizSession                             │
└─────────────────────────────────────────────┘
```

### 3.2 依赖规则

**核心原则：依赖关系只能从外向内**

- 外层可以依赖内层
- 内层不能依赖外层
- 实体层不依赖任何东西
- Use Cases只依赖实体
- Interface Adapters依赖Use Cases和实体
- Frameworks & Drivers依赖所有内层

这种设计确保：
- **业务逻辑独立于UI**：可以轻松更换UI框架
- **业务逻辑独立于数据库**：可以轻松更换数据存储方式
- **易于测试**：可以独立测试每一层
- **易于维护**：修改一层不会影响其他层

---

## 4. 依赖关系详解

### 4.1 第一层：实体层 (Entities)

**位置**: `src/main/java/com/csc207/arcade/multiplechoice/entities/`

**组件**:
- `QuizQuestion.java` - 表示单个题目
- `QuizSession.java` - 管理测验会话状态

**依赖**: 无外部依赖（纯Java对象）

**职责**:
- 封装业务规则
- 存储业务数据
- 提供数据访问方法

```
QuizQuestion（题目实体）
├── questionId: String (题目ID)
├── imagePath: String (图片路径)
├── level: int (难度等级)
└── correctAnswer: String (正确答案)

QuizSession（会话实体）
├── questions: List<QuizQuestion> (题目列表)
├── currentQuestionIndex: int (当前题目索引)
├── correctAnswersCount: int (正确答案数)
├── startTime: long (开始时间)
├── endTime: long (结束时间)
└── currentQuestionAnsweredIncorrectly: boolean (当前题是否答错过)
```

### 4.2 第二层：用例层 (Use Cases)

**位置**: `src/main/java/com/csc207/arcade/multiplechoice/use_case/`

**组件结构**:
```
use_case/
├── QuestionRepository.java (接口)
├── quiz/
│   ├── QuizInputBoundary.java (接口)
│   ├── QuizOutputBoundary.java (接口)
│   ├── QuizInputData.java (数据传输对象)
│   ├── QuizOutputData.java (数据传输对象)
│   └── QuizInteractor.java (实现)
└── submit/
    ├── SubmitAnswerInputBoundary.java (接口)
    ├── SubmitAnswerOutputBoundary.java (接口)
    ├── SubmitAnswerInputData.java (数据传输对象)
    ├── SubmitAnswerOutputData.java (数据传输对象)
    └── SubmitAnswerInteractor.java (实现)
```

**依赖关系**:
- **依赖**: Entities层（QuizQuestion, QuizSession）
- **被依赖**: Interface Adapters层

**QuizInteractor的工作流程**:
1. 从QuestionRepository获取15道题目
2. 创建新的QuizSession
3. 获取第一道题目
4. 通过QuizOutputBoundary将数据传递给Presenter

**SubmitAnswerInteractor的工作流程**:
1. 接收用户选择的答案
2. 与正确答案比较
3. 更新QuizSession状态
4. 如果正确，准备进入下一题
5. 如果错误，通知Presenter显示红色反馈
6. 如果所有题目完成，触发结果显示

### 4.3 第三层：接口适配层 (Interface Adapters)

**位置**: `src/main/java/com/csc207/arcade/multiplechoice/interface_adapter/`

**组件**:
- `QuizController.java` - 控制器，处理用户输入
- `QuizPresenter.java` - 展示器，格式化输出数据
- `QuizViewModel.java` - 视图模型，存储UI状态
- `ResultsViewModel.java` - 结果视图模型

**依赖关系**:
- **依赖**: Use Cases层和Entities层
- **被依赖**: Frameworks & Drivers层（View）

**数据流向**:
```
Controller → Interactor → Presenter → ViewModel → View
    ↑                                              ↓
    └──────────────── 用户交互 ──────────────────────┘
```

**QuizViewModel的属性**:
- `currentImagePath` - 当前题目图片路径
- `questionProgressLabel` - 进度标签（如"Question 5/15"）
- `feedbackState` - 反馈状态（NONE/CORRECT/INCORRECT）
- `incorrectButton` - 用户点击的按钮（用于着色）

所有属性的变化都会触发`PropertyChangeEvent`，通知View更新UI。

### 4.4 第四层：框架与驱动层 (Frameworks & Drivers)

**位置**: `src/main/java/com/csc207/arcade/multiplechoice/view/` 和 `data_access/`

**组件**:

**视图组件** (`view/`):
- `QuizView.java` - 主测验窗口（JFrame）
- `ResultsView.java` - 结果显示窗口
- `ScaledImagePanel.java` - 自定义图片缩放面板

**数据访问组件** (`data_access/`):
- `JsonQuestionRepository.java` - JSON文件读取实现

**依赖关系**:
- **依赖**: 所有内层
- **被依赖**: 无（最外层）

**QuizView的工作机制**:

1. **初始化**:
   - 创建4个按钮（A, B, C, D）
   - 创建图片显示面板
   - 创建进度标签
   - 注册为ViewModel的监听器

2. **响应ViewModel变化** (`propertyChange方法`):
   - `imagePath`变化 → 更新图片显示
   - `questionProgressLabel`变化 → 更新进度文本
   - `feedbackState`变化 → 改变按钮颜色
     - CORRECT → 将用户点击的按钮变绿，1秒后自动进入下一题
     - INCORRECT → 将用户点击的按钮变红
     - NONE → 重置所有按钮为默认颜色

3. **用户交互**:
   - 用户点击按钮 → 调用 `controller.submitAnswer(answer)`

**JsonQuestionRepository的工作机制**:
- 读取 `src/main/resources/data/questions.json`
- 使用GSON库解析JSON
- 将JSON反序列化为 `List<QuizQuestion>`
- 提供随机抽取题目的功能

---

## 5. 数据流程

### 5.1 完整的数据流（从启动到结束）

```
1. 启动阶段
   Main.main()
   └→ DataInitializer.run()
      └→ 扫描images文件夹
         └→ 生成questions.json

2. 初始化阶段
   Main.main()
   └→ 创建Repository, ViewModels, Presenter, Interactors, Controller
      └→ 创建View并显示
         └→ quizInteractor.execute()
            └→ repository.getQuestions(15)
               └→ 创建QuizSession
                  └→ presenter.prepareQuizView()
                     └→ viewModel.setCurrentImagePath()
                        └→ View显示第一题

3. 答题阶段（循环）
   用户点击按钮A/B/C/D
   └→ controller.submitAnswer("A")
      └→ submitAnswerInteractor.execute()
         └→ 检查答案
            ├→ 正确：
            │  └→ session.recordAnswer(true)
            │     └→ presenter.prepareSuccessView()
            │        └→ viewModel.setFeedbackState("CORRECT")
            │           └→ View显示绿色
            │              └→ 1秒后controller.advanceToNextQuestion()
            │                 └→ interactor.advanceToNextQuestion()
            │                    ├→ 有下一题：重复步骤2
            │                    └→ 无下一题：显示结果
            └→ 错误：
               └→ session.recordAnswer(false)
                  └→ presenter.prepareFailView()
                     └→ viewModel.setFeedbackState("INCORRECT")
                        └→ View显示红色
                           └→ 等待用户再次点击

4. 结束阶段
   所有题目答完
   └→ session.finishQuiz()
      └→ presenter.prepareResultsView()
         └→ resultsViewModel.setAccuracy() & setTotalTimeMs()
            └→ ResultsView显示结果
```

### 5.2 关键的设计模式

**1. Observer Pattern（观察者模式）**
- ViewModel使用`PropertyChangeSupport`
- View实现`PropertyChangeListener`
- ViewModel状态变化时自动通知View

**2. Dependency Inversion（依赖倒置）**
- Interactor依赖接口（OutputBoundary），而不是具体实现
- Repository是接口，具体实现可替换

**3. Single Responsibility（单一职责）**
- 每个类只负责一件事
- Controller负责协调
- Presenter负责格式化
- ViewModel负责存储
- View负责显示

---

## 6. 核心组件说明

### 6.1 QuizSession（会话管理器）

**关键功能**：
- 跟踪当前题目索引
- 记录答题情况
- 计算准确率和用时

**重要实现细节**：
```java
// 只在第一次尝试时记录答案
public void recordAnswer(boolean isCorrect) {
    if (!currentQuestionAnsweredIncorrectly) {
        if (isCorrect) {
            correctAnswersCount++;
        } else {
            currentQuestionAnsweredIncorrectly = true;
        }
    }
}
```

这个设计确保：
- 用户第一次答错后，即使后续答对也不计入正确数
- 准确率反映的是"第一次尝试的成功率"
- 这是在PR #6中修复的重要问题

### 6.2 ScaledImagePanel（图片缩放面板）

**核心功能**：自动缩放图片以适应窗口大小

**实现原理**：
```java
@Override
protected void paintComponent(Graphics g) {
    super.paintComponent(g);
    if (image != null) {
        // 关键：使用getWidth()和getHeight()自动缩放
        g.drawImage(image, 0, 0, getWidth(), getHeight(), null);
    }
}
```

### 6.3 DataInitializer（数据初始化器）

**文件名解析规则**：
```
格式: {id}_level{难度}_answer{答案}.png
示例: id001_level1_answerB.png
解析结果:
  - questionId: "id001"
  - level: 1
  - correctAnswer: "B"
  - imagePath: "data/images/id001_level1_answerB.png"
```

**正则表达式**：
```java
Pattern pattern = Pattern.compile("(id\\d+)_level(\\d+)_answer([ABCD])\\.png");
```

---

## 7. 更新历史

### 7.1 项目初始化（早期版本）

**创建时间**: 2025年11月之前

**初始功能**:
- 实现了完整的Clean Architecture架构
- 创建了所有必要的层和组件
- 实现了基本的测验流程
- 添加了15个示例题目

**初始设计要点**:
- 严格遵循Clean Architecture原则
- 使用GSON进行JSON解析
- 采用Java Swing构建UI
- 实现了自动图片缩放功能

### 7.2 重大更新：准确率计算修复（PR #6）

**更新时间**: 2025年11月16日  
**提交者**: Jiayi Yang  
**合并提交**: `892bc79`

**问题描述**:
在之前的版本中，准确率的计算存在问题：
- 用户第一次答错后，如果再次尝试并答对，系统会将其计入正确答案
- 这导致准确率不能真实反映用户的实际水平

**解决方案**:
修改了 `QuizSession.java` 中的 `recordAnswer` 方法：

```java
// 修改前：每次答对都计数
public void recordAnswer(boolean isCorrect) {
    if (isCorrect) {
        correctAnswersCount++;
    }
}

// 修改后：只在第一次尝试时计数
private boolean currentQuestionAnsweredIncorrectly;

public void recordAnswer(boolean isCorrect) {
    if (!currentQuestionAnsweredIncorrectly) {
        if (isCorrect) {
            correctAnswersCount++;
        } else {
            currentQuestionAnsweredIncorrectly = true;
        }
    }
}
```

**影响范围**:
- `QuizSession.java`: 添加了 `currentQuestionAnsweredIncorrectly` 字段
- `advanceToNextQuestion()`: 在进入下一题时重置该字段
- 准确率计算现在更加准确和公平

**技术意义**:
- 提高了评分系统的准确性
- 确保测验结果更能反映用户的真实水平
- 体现了对用户体验的重视

### 7.3 当前版本特性总结

**核心特性**:
1. ✅ Clean Architecture设计
2. ✅ 15道随机题目
3. ✅ 图片题目显示
4. ✅ 即时视觉反馈
5. ✅ 准确的评分系统
6. ✅ 计时功能
7. ✅ 结果统计

**技术亮点**:
- 严格的分层架构
- 可扩展的设计
- 易于测试
- 良好的代码组织

---

## 8. 技术栈

### 8.1 开发语言和框架

| 技术 | 版本 | 用途 |
|------|------|------|
| Java | 11+ | 核心开发语言 |
| Maven | 3.x | 项目构建和依赖管理 |
| Java Swing | JDK内置 | GUI框架 |
| GSON | 2.10.1 | JSON解析 |
| JUnit | 4.13.2 | 单元测试 |

### 8.2 项目依赖

**核心依赖**:
```xml
<dependency>
    <groupId>com.google.code.gson</groupId>
    <artifactId>gson</artifactId>
    <version>2.10.1</version>
</dependency>
```

**测试依赖**:
```xml
<dependency>
    <groupId>junit</groupId>
    <artifactId>junit</artifactId>
    <version>4.13.2</version>
    <scope>test</scope>
</dependency>
```

### 8.3 构建配置

**编译器设置**:
- Source: Java 11
- Target: Java 11
- Encoding: UTF-8

**主类入口**:
```
com.csc207.arcade.multiplechoice.app.Main
```

---

## 9. 目录结构详解

```
multipul-choise/
├── pom.xml                          # Maven配置文件
├── README.md                        # 英文说明文档
├── RUNNING.md                       # 运行指南
├── PROGRAM_GUIDE.md                 # 本文档
├── run.sh                           # Linux/Mac运行脚本
└── src/
    └── main/
        ├── java/
        │   └── com/csc207/arcade/multiplechoice/
        │       ├── app/                      # 应用入口层
        │       │   ├── Main.java            # 程序入口
        │       │   ├── DataInitializer.java # 数据初始化
        │       │   └── ImageGenerator.java  # 图片生成工具
        │       ├── entities/                 # 实体层
        │       │   ├── QuizQuestion.java    # 题目实体
        │       │   └── QuizSession.java     # 会话实体
        │       ├── use_case/                 # 用例层
        │       │   ├── QuestionRepository.java  # 仓库接口
        │       │   ├── quiz/                # 测验用例
        │       │   │   ├── QuizInputBoundary.java
        │       │   │   ├── QuizOutputBoundary.java
        │       │   │   ├── QuizInputData.java
        │       │   │   ├── QuizOutputData.java
        │       │   │   └── QuizInteractor.java
        │       │   └── submit/              # 提交答案用例
        │       │       ├── SubmitAnswerInputBoundary.java
        │       │       ├── SubmitAnswerOutputBoundary.java
        │       │       ├── SubmitAnswerInputData.java
        │       │       ├── SubmitAnswerOutputData.java
        │       │       └── SubmitAnswerInteractor.java
        │       ├── interface_adapter/        # 接口适配层
        │       │   ├── QuizController.java  # 测验控制器
        │       │   ├── QuizPresenter.java   # 测验展示器
        │       │   ├── QuizViewModel.java   # 测验视图模型
        │       │   └── ResultsViewModel.java # 结果视图模型
        │       ├── data_access/             # 数据访问层
        │       │   └── JsonQuestionRepository.java
        │       └── view/                    # 视图层
        │           ├── QuizView.java        # 测验视图
        │           ├── ResultsView.java     # 结果视图
        │           └── ScaledImagePanel.java # 图片面板
        └── resources/
            └── data/
                ├── questions.json           # 题目数据文件
                └── images/                  # 题目图片文件夹
                    ├── id001_level1_answerB.png
                    ├── id002_level3_answerC.png
                    └── ...
```

---

## 10. 设计原则和最佳实践

### 10.1 SOLID原则的应用

**S - Single Responsibility（单一职责）**
- 每个类只负责一个功能
- 例如：QuizPresenter只负责格式化数据，不处理业务逻辑

**O - Open/Closed（开放封闭）**
- 对扩展开放，对修改封闭
- 例如：可以添加新的Repository实现而不修改Use Case

**L - Liskov Substitution（里氏替换）**
- 子类可以替换父类
- 例如：任何QuestionRepository的实现都可以互换

**I - Interface Segregation（接口隔离）**
- 接口应该小而专注
- 例如：QuizInputBoundary和SubmitAnswerInputBoundary是分离的

**D - Dependency Inversion（依赖倒置）**
- 依赖抽象而非具体实现
- 例如：Interactor依赖OutputBoundary接口，而非具体的Presenter

### 10.2 代码质量保证

**命名规范**:
- 类名：大驼峰命名法（PascalCase）
- 方法名：小驼峰命名法（camelCase）
- 常量：全大写下划线分隔（UPPER_SNAKE_CASE）

**注释规范**:
- 所有公共类和方法都有Javadoc注释
- 复杂逻辑有行内注释说明

**错误处理**:
- 使用异常处理机制
- 在关键位置添加空值检查

---

## 11. 扩展和定制

### 11.1 如何添加新题目

1. 准备题目图片，命名格式：`id{编号}_level{难度}_answer{答案}.png`
2. 将图片放入 `src/main/resources/data/images/` 目录
3. 重新运行程序，DataInitializer会自动更新questions.json

### 11.2 如何修改题目数量

在 `Main.java` 中修改：
```java
// 将15改为其他数字
QuizInteractor quizInteractor = new QuizInteractor(repository, presenter);
```

然后在 `QuizInteractor` 的 `execute` 方法中修改：
```java
List<QuizQuestion> questions = questionRepository.getQuestions(15); // 修改这个数字
```

### 11.3 如何更换UI

由于采用了Clean Architecture，可以轻松更换UI框架：

1. 创建新的View类（如WebView、JavaFXView等）
2. 实现PropertyChangeListener接口
3. 在Main.java中替换QuizView即可
4. 业务逻辑层无需任何修改

### 11.4 如何更换数据源

可以替换JSON数据源为数据库或API：

1. 创建新的Repository实现（如DatabaseQuestionRepository）
2. 实现QuestionRepository接口
3. 在Main.java中替换JsonQuestionRepository
4. 其他层无需修改

---

## 12. 常见问题解答

### Q1: 为什么使用Clean Architecture？
A: Clean Architecture提供了清晰的层次划分，使代码易于理解、测试和维护。它让业务逻辑独立于框架和工具，便于长期演进。

### Q2: 为什么准确率只计算第一次尝试？
A: 这样设计是为了更准确地反映用户的知识水平。如果允许多次尝试，用户可能通过猜测得到高分，无法真实评估学习效果。

### Q3: 为什么要生成questions.json而不是直接读取图片？
A: 分离数据和文件可以提高性能，减少IO操作。同时，JSON格式便于扩展，未来可以添加更多元数据（如题目说明、标签等）。

### Q4: 能否支持多选题？
A: 当前架构支持扩展为多选题。只需修改QuizQuestion实体和SubmitAnswerInteractor的检查逻辑即可。

### Q5: 如何国际化（支持多语言）？
A: 可以使用Java的ResourceBundle机制，将所有UI文本提取到properties文件中。

---

## 13. 性能优化建议

### 13.1 当前性能特点

- 启动速度：快速（毫秒级）
- 内存占用：低（15个题目，约10-20MB）
- 响应速度：即时（UI操作无延迟）

### 13.2 可能的优化点

1. **图片加载优化**
   - 使用图片缓存
   - 预加载下一题图片

2. **数据加载优化**
   - 延迟加载题目数据
   - 使用懒加载模式

3. **内存优化**
   - 及时释放已答题目的图片资源
   - 使用弱引用管理大对象

---

## 14. 测试建议

### 14.1 单元测试

推荐为以下组件编写单元测试：
- QuizSession的状态管理逻辑
- SubmitAnswerInteractor的答案检查逻辑
- QuizPresenter的数据格式化逻辑

### 14.2 集成测试

推荐测试场景：
- 完整的答题流程
- 数据持久化和加载
- UI交互逻辑

### 14.3 测试工具

- JUnit 4：单元测试框架
- Mockito：模拟对象框架（推荐添加）
- AssertJ：断言库（推荐添加）

---

## 15. 总结

本程序是一个设计精良、架构清晰的多选题测验应用。它展示了以下优秀的软件工程实践：

1. **Clean Architecture** - 清晰的层次划分
2. **SOLID原则** - 高质量的代码设计
3. **设计模式** - Observer、Dependency Inversion等
4. **可维护性** - 易于理解和修改
5. **可扩展性** - 易于添加新功能
6. **可测试性** - 各层独立可测试

通过本文档，开发者可以全面理解程序的设计思想、实现细节和演进历史，为后续的维护和扩展打下坚实基础。

---

## 附录

### A. 相关链接

- [Clean Architecture文章（Uncle Bob）](https://blog.cleancoder.com/uncle-bob/2012/08/13/the-clean-architecture.html)
- [SOLID原则详解](https://en.wikipedia.org/wiki/SOLID)
- [Java Swing教程](https://docs.oracle.com/javase/tutorial/uiswing/)
- [GSON文档](https://github.com/google/gson)

### B. 贡献者

- Jiayi Yang - 准确率计算修复
- copilot-swe-agent[bot] - 文档维护

### C. 许可证

本项目遵循CSC207课程项目的许可要求。

---

**文档版本**: 1.0  
**最后更新**: 2025年11月17日  
**维护者**: GitHub Copilot
