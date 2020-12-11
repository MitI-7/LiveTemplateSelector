import com.intellij.codeInsight.template.Template;
import com.intellij.codeInsight.template.TemplateManager;
import com.intellij.codeInsight.template.impl.TemplateSettings;
import com.intellij.openapi.editor.Editor;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import java.awt.event.*;
import com.intellij.ui.treeStructure.Tree;


public class SelectLiveTemplate extends JDialog {
    private JPanel contentPane;
    private JTree tree;
    private TemplateManager templateManager;
    private TemplateSettings templateSettings;
    private Editor editor;
    
    
    public SelectLiveTemplate(@NotNull DefaultMutableTreeNode rootNode, TemplateManager templateManager, TemplateSettings templateSettings, Editor editor) {
        super();
        this.templateManager = templateManager;
        this.templateSettings = templateSettings;
        this.editor = editor;

        this.tree.setModel(new DefaultTreeModel(rootNode));
        this.tree.setRootVisible(true);
        setContentPane(contentPane);
        setSize(500, 354);
        setUndecorated(true);
        
        // call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

        this.addWindowFocusListener(new WindowFocusListener() {
            @Override
            public void windowGainedFocus(WindowEvent e) {
            }

            @Override
            public void windowLostFocus(WindowEvent e) {
                dispose();
            }
        });

        // call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(event -> onCancel(),
                                           KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                                           JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        // マウスで選択したとき
        tree.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                TreePath treePath = tree.getPathForLocation(e.getX(), e.getY());
                if (treePath == null) {
                    return;
                }
                
                // leaf
                if (treePath.getPath().length == 3) {
                    String groupName = (String) ((DefaultMutableTreeNode)treePath.getPath()[1]).getUserObject();
                    String templateName = (String) ((DefaultMutableTreeNode)treePath.getPath()[2]).getUserObject();
                    insertTemplate(groupName, templateName);
                    dispose();
                }
            }
        });

        // キーボードで選択したとき
        tree.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                super.keyReleased(e);
                if (e.getKeyCode() != KeyEvent.VK_ENTER) {
                    return;
                }
                JTree tree = (JTree) e.getSource();
                TreePath treePath = tree.getLeadSelectionPath();
                
                if (treePath == null) {
                    return;
                }

                // leaf
                if (treePath.getPath().length == 3) {
                    String groupName = (String) ((DefaultMutableTreeNode)treePath.getPath()[1]).getUserObject();
                    String templateName = (String) ((DefaultMutableTreeNode)treePath.getPath()[2]).getUserObject();
                    insertTemplate(groupName, templateName);
                    dispose();
                }

                e.consume();                
            }
        });
    }
    
    private void onCancel() {
        dispose();
    }

    private void insertTemplate(String groupName, String templateName) {
        Template template = templateSettings.getTemplate(templateName, groupName);
        if (template == null) {return;}
        templateManager.startTemplate(editor, template);
    }
}
